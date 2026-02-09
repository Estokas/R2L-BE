package com.accenture.R2L.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Ollama API request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("stream")
    @Builder.Default
    private Boolean stream = false;

    @JsonProperty("format")
    private String format; // "json" for structured output
}