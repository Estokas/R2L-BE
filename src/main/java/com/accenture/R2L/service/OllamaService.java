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
import reactor.core.publisher.Mono;

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

    private static final String REVIEW_PROMPT_TEMPLATE = """
            You are an expert code reviewer. Analyze the following Git commit diff and identify potential issues.
            
            Return your findings in strict JSON format with these categories:
            - bugs: Logic errors, null pointers, incorrect implementations
            - security: Security vulnerabilities, injection risks, authentication issues
            - performance: Inefficient algorithms, memory leaks, unnecessary operations
            - style: Code style violations, naming conventions, formatting
            - refactor: Code smells, duplication, complexity issues
            
            For each finding, provide:
            {
              "file": "filename",
              "line": line_number,
              "severity": "CRITICAL|HIGH|MEDIUM|LOW|INFO",
              "description": "what is wrong",
              "suggestion": "how to fix it"
            }
            
            Return ONLY valid JSON in this exact format:
            {
              "bugs": [],
              "security": [],
              "performance": [],
              "style": [],
              "refactor": []
            }
            
            If no issues found in a category, leave it as an empty array.
            
            Commit Diff:
            %s
            """;

    /**
     * Send code diff to Ollama for AI review.
     *
     * @param diff The code diff to analyze
     * @return Parsed AI review results
     */
    public AiReviewResult analyzeCode(String diff) {
        log.info("Sending code diff to Ollama for analysis");

        try {
            String prompt = String.format(REVIEW_PROMPT_TEMPLATE, diff);

            OllamaRequest request = OllamaRequest.builder()
                    .model(model)
                    .prompt(prompt)
                    .stream(false)
                    .format("json")
                    .build();

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

            log.info("Successfully parsed {} total findings from AI", totalFindings);
            return result;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON: {}", jsonResponse, e);
            return new AiReviewResult(); // Return empty result on parse error
        }
    }
}
