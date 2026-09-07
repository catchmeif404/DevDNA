package dev.devwrapped.worker.collector;

import java.util.List;

public record RawActivity(List<GithubRepoItem> repositories, List<GithubCommitItem> commits, int pullRequestCount) {
}
