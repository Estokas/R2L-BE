package com.accenture.R2L.service;

import com.accenture.R2L.dto.GitHubCommitDiff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Service for interacting with GitHub REST API.
 * Handles fetching commit diffs and metadata.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubService {

    @Qualifier("githubWebClient")
    private final WebClient githubWebClient;

    private static final List<String> BINARY_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".ico", ".pdf",
            ".zip", ".tar", ".gz", ".rar", ".7z", ".exe", ".dll",
            ".so", ".dylib", ".class", ".jar", ".war"
    );

    /**
     * Fetch commit diff from GitHub API.
     *
     * @param owner Repository owner
     * @param repo Repository name
     * @param commitSha Commit SHA
     * @return GitHubCommitDiff containing file changes
     */
    public GitHubCommitDiff fetchCommitDiff(String owner, String repo, String commitSha) {
        log.info("Fetching commit diff for {}/{} @ {}", owner, repo, commitSha);

        try {
            GitHubCommitDiff diff = githubWebClient.get()
                    .uri("/repos/{owner}/{repo}/commits/{sha}", owner, repo, commitSha)
                    .retrieve()
                    .bodyToMono(GitHubCommitDiff.class)
                    .block();

            if (diff != null && diff.getFiles() != null) {
                // Filter out binary files
                diff.getFiles().removeIf(this::isBinaryFile);
                log.info("Retrieved {} non-binary file changes", diff.getFiles().size());
            }

            return diff;
        } catch (Exception e) {
            log.error("Error fetching commit diff from GitHub", e);
            throw new RuntimeException("Failed to fetch commit diff: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a file is binary based on extension.
     */
    private boolean isBinaryFile(GitHubCommitDiff.FileChange file) {
        String filename = file.getFilename().toLowerCase();
        return BINARY_EXTENSIONS.stream().anyMatch(filename::endsWith) || file.getPatch() == null;
    }

    /**
     * Build a formatted diff string for AI analysis.
     */
    public String buildDiffString(GitHubCommitDiff diff) {
        if (diff == null || diff.getFiles() == null || diff.getFiles().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (GitHubCommitDiff.FileChange file : diff.getFiles()) {
            sb.append("File: ").append(file.getFilename()).append("\n");
            sb.append("Status: ").append(file.getStatus()).append("\n");
            sb.append("Changes: +").append(file.getAdditions())
                    .append(" -").append(file.getDeletions()).append("\n");

            if (file.getPatch() != null) {
                sb.append("Diff:\n").append(file.getPatch()).append("\n");
            }
            sb.append("\n---\n\n");
        }
        return sb.toString();
    }
}
