package dev.devwrapped.backend.collector;

import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Runs collection with retry + exponential backoff (section 7.2 / 21). */
@Service
public class CollectorService {

    private static final Logger log = LoggerFactory.getLogger(CollectorService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 1000;

    private final GithubApiClient client;

    public CollectorService(GithubApiClient client) {
        this.client = client;
    }

    public RawActivity collect(String username, String accessToken) {
        List<GithubRepoItem> repos = withRetry(() -> client.fetchOwnedRepositories(username, accessToken));
        List<GithubCommitItem> commits = withRetry(() -> client.searchCommits(username, accessToken));
        int prCount = withRetry(() -> client.countPullRequests(username, accessToken));
        return new RawActivity(repos, commits, prCount);
    }

    private <T> T withRetry(Supplier<T> call) {
        GithubApiException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (GithubApiException e) {
                lastError = e;
                if (!e.isRetryable() || attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                long backoff = BASE_BACKOFF_MS * (1L << (attempt - 1));
                log.warn("GitHub API call failed (attempt {}/{}), retrying in {}ms: {}", attempt, MAX_ATTEMPTS,
                        backoff, e.getMessage());
                sleep(backoff);
            }
        }
        throw lastError;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
