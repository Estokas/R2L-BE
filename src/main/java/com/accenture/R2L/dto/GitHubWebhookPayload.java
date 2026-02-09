package com.accenture.R2L.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for GitHub push webhook payload.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookPayload {

    @JsonProperty("ref")
    private String ref;

    @JsonProperty("repository")
    private Repository repository;

    @JsonProperty("commits")
    private List<Commit> commits;

    @JsonProperty("pusher")
    private Pusher pusher;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("name")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        @JsonProperty("id")
        private String id;

        @JsonProperty("message")
        private String message;

        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("author")
        private Author author;

        @JsonProperty("url")
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        @JsonProperty("name")
        private String name;

        @JsonProperty("email")
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pusher {
        @JsonProperty("name")
        private String name;
    }
}