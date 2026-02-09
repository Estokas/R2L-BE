
package com.accenture.R2L.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for WebClient instances used for external API calls.
 * Separate clients for GitHub and Ollama with appropriate settings.
 */
@Configuration
public class WebClientConfig {

    @Value("${app.github.api-url:https://api.github.com}")
    private String githubApiUrl;

    @Value("${app.github.token:}")
    private String githubToken;

    @Value("${app.ollama.api-url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${app.ollama.timeout:120000}")
    private long ollamaTimeout;

    @Bean(name = "githubWebClient")
    public WebClient githubWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(githubApiUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

        // Only add Authorization header if token is provided
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        }

        return builder.build();
    }

    @Bean(name = "ollamaWebClient")
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl(ollamaApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer for large responses
                .build();
    }
}