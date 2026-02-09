package com.accenture.R2L.domain;

/**
 * Status of a code review process.
 */
public enum ReviewStatus {
    PENDING,      // Webhook received, queued for processing
    PROCESSING,   // Currently being analyzed
    COMPLETED,    // Successfully completed
    FAILED        // Error occurred during processing
}
