package dev.devwrapped.backend.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Backend's own Jackson config stays default (camelCase) for its own API responses to the
// frontend, so - unlike the old worker, which set a global SNAKE_CASE naming strategy - the
// snake_case GitHub API fields are mapped individually here instead.
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoItem(
        String name,
        String language,
        @JsonProperty("stargazers_count") Integer stargazersCount,
        Boolean fork,
        @JsonProperty("created_at") String createdAt) {
}
