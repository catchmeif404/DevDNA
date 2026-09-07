package dev.devwrapped.worker.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommitItem(CommitInfo commit, RepositoryInfo repository) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitInfo(String message, CommitAuthorInfo author) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitAuthorInfo(String date) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepositoryInfo(String fullName) {
    }
}
