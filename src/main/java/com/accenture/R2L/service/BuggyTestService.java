//package com.accenture.R2L.test;
//
//import com.accenture.R2L.domain.CodeReview;
//import java.util.List;
//
///**
// * INTENTIONALLY BUGGY CODE FOR AI REVIEW TESTING
// * This class contains multiple common bugs and issues.
// */
//public class BuggyTestService {
//
//    // BUG 1: Potential NullPointerException
//    public String getUserEmail(CodeReview review) {
//        return review.getAuthor().toLowerCase(); // What if author is null?
//    }
//
//    // BUG 2: SQL Injection vulnerability
//    public void deleteReview(String commitSha) {
//        String sql = "DELETE FROM code_reviews WHERE commit_sha = '" + commitSha + "'";
//        // Direct string concatenation = SQL injection risk!
//    }
//
//    // BUG 3: No error handling
//    public void processReviews(List<CodeReview> reviews) {
//        for (CodeReview review : reviews) {
//            review.getFindings().get(0).getDescription(); // May throw IndexOutOfBoundsException
//        }
//    }
//
//    // BUG 4: Hardcoded credentials
//    private static final String API_KEY = "sk_live_1234567890abcdef";
//    private static final String DB_PASSWORD = "admin123";
//
//    // BUG 5: Resource leak
//    public void readFile(String path) {
//        java.io.FileInputStream fis = new java.io.FileInputStream(path); // Never closed!
//        // Missing try-with-resources or finally block
//    }
//
//    // BUG 6: Thread safety issue
//    private int counter = 0;
//    public void incrementCounter() {
//        counter++; // Not thread-safe!
//    }
//
//    // BUG 7: Inefficient algorithm (N+1 problem)
//    public void printAllReviewAuthors(List<CodeReview> reviews) {
//        for (CodeReview review : reviews) {
//            // Each iteration could trigger separate DB query
//            System.out.println(review.getFindings().size());
//        }
//    }
//
//    // BUG 8: Magic numbers
//    public boolean isValidReview(CodeReview review) {
//        return review.getFindings().size() < 50; // What is 50?
//    }
//
//    // BUG 9: Empty catch block
//    public void riskyOperation() {
//        try {
//            // Some risky code
//            Thread.sleep(1000);
//        } catch (Exception e) {
//            // Silently swallowing exception!
//        }
//    }
//
//    // BUG 10: Deprecated method usage
//    public void oldMethod() {
//        new java.util.Date().getYear(); // Deprecated!
//    }
//}