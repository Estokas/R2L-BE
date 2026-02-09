package com.accenture.R2L.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a code review performed on a Git commit.
 * Contains metadata about the commit and AI-generated findings.
 */
@Entity
@Table(name = "code_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String commitSha;

    @Column(nullable = false)
    private String repositoryName;

    @Column(nullable = false)
    private String author;

    @Column(length = 1000)
    private String commitMessage;

    @Column(nullable = false)
    private LocalDateTime commitTimestamp;

    @Column(nullable = false)
    private LocalDateTime reviewTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ReviewFinding> findings = new ArrayList<>();

    @Column(length = 2000)
    private String errorMessage;

    /**
     * Helper method to add a finding to this review.
     */
    public void addFinding(ReviewFinding finding) {
        findings.add(finding);
        finding.setCodeReview(this);
    }
}