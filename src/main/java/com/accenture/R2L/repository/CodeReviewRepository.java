package com.accenture.R2L.repository;


import com.accenture.R2L.domain.CodeReview;
import com.accenture.R2L.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CodeReview entity operations.
 */
@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    /**
     * Find a code review by commit SHA.
     */
    Optional<CodeReview> findByCommitSha(String commitSha);

    /**
     * Check if a review exists for a commit.
     */
    boolean existsByCommitSha(String commitSha);

    /**
     * Find all reviews for a repository, ordered by timestamp.
     */
    List<CodeReview> findByRepositoryNameOrderByCommitTimestampDesc(String repositoryName);

    /**
     * Find reviews by status.
     */
    List<CodeReview> findByStatus(ReviewStatus status);

    /**
     * Find all reviews ordered by most recent first.
     */
    List<CodeReview> findAllByOrderByReviewTimestampDesc();
}
