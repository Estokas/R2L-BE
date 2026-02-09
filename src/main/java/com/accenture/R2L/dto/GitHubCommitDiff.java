package com.accenture.R2L.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for GitHub commit diff response.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitDiff {

    @JsonProperty("sha")
    private String sha;

    @JsonProperty("files")
    private List<FileChange> files;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileChange {
        @JsonProperty("filename")
        private String filename;

        @JsonProperty("status")
        private String status;

        @JsonProperty("additions")
        private Integer additions;

        @JsonProperty("deletions")
        private Integer deletions;

        @JsonProperty("patch")
        private String patch;
    }
}