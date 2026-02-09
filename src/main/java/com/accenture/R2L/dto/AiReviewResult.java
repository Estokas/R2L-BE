package com.accenture.R2L.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for parsed AI review results from Ollama.
 */
@Data
public class AiReviewResult {

    private List<Finding> bugs = new ArrayList<>();
    private List<Finding> security = new ArrayList<>();
    private List<Finding> performance = new ArrayList<>();
    private List<Finding> style = new ArrayList<>();
    private List<Finding> refactor = new ArrayList<>();

    @Data
    public static class Finding {
        private String file;
        private Integer line;
        private String severity;
        private String description;
        private String suggestion;
    }
}