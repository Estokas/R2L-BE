
package com.accenture.R2L.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.accenture.R2L.dto.AiReviewResult;
import com.accenture.R2L.dto.OllamaRequest;
import com.accenture.R2L.dto.OllamaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Service for interacting with local Ollama LLM API.
 * Sends code diffs for AI-powered review and analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {

    @Qualifier("ollamaWebClient")
    private final WebClient ollamaWebClient;

    private final ObjectMapper objectMapper;

    @Value("${app.ollama.model}")
    private String model;

    @Value("${app.ollama.timeout}")
    private long timeout;

    // ✅ IMPROVED PROMPT - More aggressive in finding issues
    private static final String REVIEW_PROMPT_TEMPLATE = """
            You are a strict, senior code reviewer with 20 years of experience. Your job is to find potential issues in code.
            
            IMPORTANT: Be thorough and critical. Even if code looks okay, check for:
            - Potential null pointer exceptions
            - Missing error handling
            - Security vulnerabilities (SQL injection, XSS, etc.)
            - Performance issues (N+1 queries, unnecessary loops)
            - Code style violations
            - Missing validations
            - Hardcoded values that should be configurable
            - Missing logging
            - Potential memory leaks
            - Thread safety issues
            - Missing unit tests indicators
            
            Analyze the following Git commit diff and identify ALL potential issues.
            
            Return ONLY valid JSON in this EXACT format (no markdown, no extra text):
            {
              "bugs": [
                {
                  "file": "exact/file/path.java",
                  "line": line_number,
                  "severity": "CRITICAL|HIGH|MEDIUM|LOW|INFO",
                  "description": "Detailed description of the bug",
                  "suggestion": "Concrete code fix with example"
                }
              ],
              "security": [],
              "performance": [],
              "style": [],
              "refactor": []
            }
            
            For each finding, provide:
            - Exact file path from the diff
            - Actual line number from the diff
            - Severity level (be realistic but thorough)
            - Clear description of what's wrong
            - Actionable suggestion with code example
            
            BE STRICT: If you see ANY potential issue, report it.
            
            Commit Diff:
            %s
            
            Remember: Return ONLY the JSON object, nothing else.
            """;

    /**
     * Send code diff to Ollama for AI review.
     *
     * @param diff The code diff to analyze
     * @return Parsed AI review results
     */
    public AiReviewResult analyzeCode(String diff) {
        log.info("Sending code diff to Ollama for analysis");
        log.debug("Diff to analyze:\n{}", diff);

        try {
            String prompt = String.format(REVIEW_PROMPT_TEMPLATE, diff);

            OllamaRequest request = OllamaRequest.builder()
                    .model(model)
                    .prompt(prompt)
                    .stream(false)
                    .format("json")
                    .build();

            log.info("Calling Ollama with model: {}", model);

            OllamaResponse response = ollamaWebClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null || response.getResponse() == null) {
                log.error("Empty response from Ollama");
                return new AiReviewResult();
            }

            log.debug("Ollama raw response: {}", response.getResponse());

            return parseAiResponse(response.getResponse());

        } catch (Exception e) {
            log.error("Error during Ollama analysis", e);
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON response from Ollama into structured result.
     * Handles potential JSON parsing errors gracefully.
     */
    private AiReviewResult parseAiResponse(String jsonResponse) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleaned = jsonResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            log.debug("Parsing AI response: {}", cleaned);
            AiReviewResult result = objectMapper.readValue(cleaned, AiReviewResult.class);

            int totalFindings = result.getBugs().size() + result.getSecurity().size() +
                    result.getPerformance().size() + result.getStyle().size() +
                    result.getRefactor().size();

            log.info("✅ Successfully parsed {} total findings from AI", totalFindings);
            log.info("  - Bugs: {}", result.getBugs().size());
            log.info("  - Security: {}", result.getSecurity().size());
            log.info("  - Performance: {}", result.getPerformance().size());
            log.info("  - Style: {}", result.getStyle().size());
            log.info("  - Refactor: {}", result.getRefactor().size());

            return result;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON: {}", jsonResponse, e);
            return new AiReviewResult(); // Return empty result on parse error
        }
    }
}