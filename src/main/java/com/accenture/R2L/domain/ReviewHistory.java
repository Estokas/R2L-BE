package com.accenture.R2L.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    private FindingType type;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(length = 2000)
    private String originalIssue;

    @Column(length = 2000)
    private String suggestedFix;

    @Column(length = 2000)
    private String actualFix;

    @Column(nullable = false)
    private String originalCommitSha;

    private String fixCommitSha;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    private LocalDateTime fixedAt;

    @Column(nullable = false)
    private boolean isFixed;

    @Column(nullable = false)
    private String repositoryName;

    @Column(nullable = false)
    private String author;
}