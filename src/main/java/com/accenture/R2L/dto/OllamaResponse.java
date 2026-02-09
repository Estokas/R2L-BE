package com.accenture.R2L.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Ollama API response.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaResponse {

    @JsonProperty("model")
    private String model;

    @JsonProperty("response")
    private String response;

    @JsonProperty("done")
    private Boolean done;
}