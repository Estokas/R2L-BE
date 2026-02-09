package com.accenture.R2L.service;

import com.accenture.R2L.domain.*;
import com.accenture.R2L.dto.AiReviewResult;
import com.accenture.R2L.dto.GitHubCommitDiff;
import com.accenture.R2L.dto.GitHubWebhookPayload;
import com.accenture.R2L.repository.CodeReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Core business logic service for code reviews.
 * Orchestrates webhook processing, Git operations, AI analysis, and persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewService {

    private final CodeReviewRepository codeReviewRepository;
    private final GitHubService gitHubService;
    private final OllamaService ollamaService;

    /**
     * Process a GitHub webhook push event asynchronously.
     * Creates pending review records and triggers AI analysis.
     */
    @Async("taskExecutor")
    @Transactional
    public void processWebhook(GitHubWebhookPayload payload) {
        log.info("Processing webhook for repository: {}", payload.getRepository().getFullName());

        if (payload.getCommits() == null || payload.getCommits().isEmpty()) {
            log.warn("No commits found in webhook payload");
            return;
        }

        for (GitHubWebhookPayload.Commit commit : payload.getCommits()) {
            try {
                processCommit(payload.getRepository(), commit);
            } catch (Exception e) {
                log.error("Error processing commit {}", commit.getId(), e);
            }
        }
    }

    /**
     * Process a single commit: fetch diff, analyze with AI, store results.
     */
    @Transactional
    public void processCommit(GitHubWebhookPayload.Repository repository,
                              GitHubWebhookPayload.Commit commit) {

        String commitSha = commit.getId();

        // Check if already processed
        if (codeReviewRepository.existsByCommitSha(commitSha)) {
            log.info("Commit {} already reviewed, skipping", commitSha);
            return;
        }

        // Create pending review record
        CodeReview review = createPendingReview(repository, commit);
        review = codeReviewRepository.save(review);

        try {
            // Update status to processing
            review.setStatus(ReviewStatus.PROCESSING);
            codeReviewRepository.save(review);

            // Fetch commit diff from GitHub
            String[] parts = repository.getFullName().split("/");
            String owner = parts[0];
            String repo = parts[1];

            GitHubCommitDiff diff = gitHubService.fetchCommitDiff(owner, repo, commitSha);
            String diffString = gitHubService.buildDiffString(diff);

            if (diffString.isEmpty()) {
                log.warn("No analyzable changes found for commit {}", commitSha);
                review.setStatus(ReviewStatus.COMPLETED);
                codeReviewRepository.save(review);
                return;
            }

            // Analyze with AI
            AiReviewResult aiResult = ollamaService.analyzeCode(diffString);

            // Convert AI results to entities
            convertAndStoreFindings(review, aiResult);

            // Mark as completed
            review.setStatus(ReviewStatus.COMPLETED);
            review.setReviewTimestamp(LocalDateTime.now());
            codeReviewRepository.save(review);

            log.info("Successfully completed review for commit {}", commitSha);

        } catch (Exception e) {
            log.error("Failed to process commit {}", commitSha, e);
            review.setStatus(ReviewStatus.FAILED);
            review.setErrorMessage(e.getMessage());
            codeReviewRepository.save(review);
        }
    }

    /**
     * Create a pending code review record from webhook data.
     */
    private CodeReview createPendingReview(GitHubWebhookPayload.Repository repository,
                                           GitHubWebhookPayload.Commit commit) {
        return CodeReview.builder()
                .commitSha(commit.getId())
                .repositoryName(repository.getFullName())
                .author(commit.getAuthor().getName())
                .commitMessage(commit.getMessage())
                .commitTimestamp(parseTimestamp(commit.getTimestamp()))
                .reviewTimestamp(LocalDateTime.now())
                .status(ReviewStatus.PENDING)
                .build();
    }

    /**
     * Convert AI findings to domain entities and attach to review.
     */
    private void convertAndStoreFindings(CodeReview review, AiReviewResult aiResult) {
        processFindingList(review, aiResult.getBugs(), FindingType.BUGS);
        processFindingList(review, aiResult.getSecurity(), FindingType.SECURITY);
        processFindingList(review, aiResult.getPerformance(), FindingType.PERFORMANCE);
        processFindingList(review, aiResult.getStyle(), FindingType.STYLE);
        processFindingList(review, aiResult.getRefactor(), FindingType.REFACTOR);
    }

    /**
     * Process a list of findings of a specific type.
     */
    private void processFindingList(CodeReview review,
                                    List<AiReviewResult.Finding> findings,
                                    FindingType type) {
        for (AiReviewResult.Finding finding : findings) {
            ReviewFinding entity = ReviewFinding.builder()
                    .type(type)
                    .fileName(finding.getFile() != null ? finding.getFile() : "unknown")
                    .lineNumber(finding.getLine() != null ? finding.getLine() : 0)
                    .severity(parseSeverity(finding.getSeverity()))
                    .description(finding.getDescription())
                    .suggestion(finding.getSuggestion())
                    .build();

            review.addFinding(entity);
        }
    }

    /**
     * Parse severity string to enum, with fallback to MEDIUM.
     */
    private Severity parseSeverity(String severity) {
        if (severity == null) return Severity.MEDIUM;
        try {
            return Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Severity.MEDIUM;
        }
    }

    /**
     * Parse ISO timestamp from GitHub.
     */
    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // ===== Public API Methods =====

    /**
     * Get all code reviews, most recent first.
     */
    @Transactional(readOnly = true)
    public List<CodeReview> getAllReviews() {
        return codeReviewRepository.findAllByOrderByReviewTimestampDesc();
    }

    /**
     * Get a specific code review by commit SHA.
     */
    @Transactional(readOnly = true)
    public Optional<CodeReview> getReviewByCommitSha(String commitSha) {
        return codeReviewRepository.findByCommitSha(commitSha);
    }

    /**
     * Get reviews for a specific repository.
     */
    @Transactional(readOnly = true)
    public List<CodeReview> getReviewsByRepository(String repositoryName) {
        return codeReviewRepository.findByRepositoryNameOrderByCommitTimestampDesc(repositoryName);
    }
}
