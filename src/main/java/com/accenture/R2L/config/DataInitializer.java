package com.accenture.R2L.config;

import com.accenture.R2L.domain.*;
import com.accenture.R2L.repository.CodeReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CodeReviewRepository codeReviewRepository;

    @Override
    public void run(String... args) {
        // Only insert if database is empty
        if (codeReviewRepository.count() == 0) {
            log.info("Initializing database with sample data...");

            // Create sample review
            CodeReview review = CodeReview.builder()
                    .commitSha("a1cabe0e700ac62bc917d4289456ab17801086dd")
                    .repositoryName("accenture/sample-app")
                    .author("John Lloyd Denum")
                    .commitMessage("Added authentication feature")
                    .commitTimestamp(LocalDateTime.now().minusHours(2))
                    .reviewTimestamp(LocalDateTime.now().minusHours(1))
                    .status(ReviewStatus.COMPLETED)
                    .build();

            // Add findings
            ReviewFinding bug = ReviewFinding.builder()
                    .type(FindingType.BUGS)
                    .fileName("src/main/java/com/example/UserService.java")
                    .lineNumber(42)
                    .severity(Severity.HIGH)
                    .description("Potential NullPointerException when accessing user.getEmail()")
                    .suggestion("Add null check: if (user != null && user.getEmail() != null)")
                    .build();

            ReviewFinding security = ReviewFinding.builder()
                    .type(FindingType.SECURITY)
                    .fileName("src/main/java/com/example/AuthController.java")
                    .lineNumber(78)
                    .severity(Severity.CRITICAL)
                    .description("SQL Injection vulnerability in login query")
                    .suggestion("Use PreparedStatement or JPA repositories")
                    .build();

            ReviewFinding performance = ReviewFinding.builder()
                    .type(FindingType.PERFORMANCE)
                    .fileName("src/main/java/com/example/DataService.java")
                    .lineNumber(125)
                    .severity(Severity.MEDIUM)
                    .description("N+1 query problem detected")
                    .suggestion("Use JOIN FETCH to load related entities")
                    .build();

            review.addFinding(bug);
            review.addFinding(security);
            review.addFinding(performance);

            codeReviewRepository.save(review);

            log.info("✅ Sample data initialized successfully!");
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }
}