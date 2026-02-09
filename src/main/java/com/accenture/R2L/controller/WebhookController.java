
package com.accenture.R2L.controller;

import com.accenture.R2L.dto.GitHubWebhookPayload;
import com.accenture.R2L.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for receiving GitHub webhook events.
 * Minimal business logic - delegates to service layer.
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final CodeReviewService codeReviewService;

    /**
     * Endpoint to receive GitHub push webhook events.
     * Returns immediately with 202 Accepted while processing asynchronously.
     *
     * @param payload GitHub webhook payload
     * @return Accepted response
     */
    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestBody GitHubWebhookPayload payload) {
        log.info("Received GitHub webhook for repository: {}",
                payload.getRepository() != null ? payload.getRepository().getFullName() : "unknown");

        try {
            // Async processing - returns immediately
            codeReviewService.processWebhook(payload);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Webhook received and queued for processing");
        } catch (Exception e) {
            log.error("Error handling webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for webhook URL.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint is active");
    }
}