package dev.devwrapped.worker.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoItem(String name, String language, Integer stargazersCount, Boolean fork,
        String createdAt) {
}
