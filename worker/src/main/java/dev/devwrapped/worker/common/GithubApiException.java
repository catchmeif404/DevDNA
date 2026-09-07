package dev.devwrapped.worker.common;

/** Thrown for GitHub API failures. {@code retryable} drives the collector's backoff loop (section 21). */
public class GithubApiException extends RuntimeException {

    private final boolean retryable;

    public GithubApiException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
