package dev.devwrapped.backend.collector;

import java.util.List;

public record RawActivity(List<GithubRepoItem> repositories, List<GithubCommitItem> commits,
        int pullRequestCount, Observation observation) {
    public RawActivity(List<GithubRepoItem> repositories, List<GithubCommitItem> commits,
            int pullRequestCount) {
        this(repositories, commits, pullRequestCount, Observation.empty());
    }
}
