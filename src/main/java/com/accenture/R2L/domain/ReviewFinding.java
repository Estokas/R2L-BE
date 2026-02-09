package com.accenture.R2L.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a single finding from AI code review.
 * Categorized by type (bug, security, performance, style, refactor).
 */
@Entity
@Table(name = "review_findings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    @JsonIgnore
    private CodeReview codeReview;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingType type;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false)
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 2000)
    private String suggestion;
}
