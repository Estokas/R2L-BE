
package com.accenture.R2L.controller;

import com.accenture.R2L.domain.CodeReview;
import com.accenture.R2L.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for code review queries.
 * Provides API endpoints for the Angular dashboard.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final CodeReviewService codeReviewService;

    /**
     * GET /api/reviews
     * Retrieve all code reviews, most recent first.
     *
     * @return List of all code reviews with findings
     */
    @GetMapping
    public ResponseEntity<List<CodeReview>> getAllReviews() {
        log.info("Fetching all code reviews");
        List<CodeReview> reviews = codeReviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/{commitSha}
     * Retrieve a specific code review by commit SHA.
     *
     * @param commitSha Git commit SHA
     * @return Code review with findings, or 404 if not found
     */
    @GetMapping("/{commitSha}")
    public ResponseEntity<CodeReview> getReviewByCommitSha(@PathVariable String commitSha) {
        log.info("Fetching review for commit: {}", commitSha);
        return codeReviewService.getReviewByCommitSha(commitSha)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}