package dev.devwrapped.backend.collector;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin GitHub REST API client. Uses the Search API for commits/PRs instead of iterating every
 * repository (section 9) — far fewer requests, and reflects public activity across all of GitHub,
 * not just the user's own repos.
 *
 * <p>Every analysis currently calls in with {@code accessToken = null} (the logged-in user's own
 * OAuth token is deliberately never persisted/reused - section 6/20), which means every request
 * shares GitHub's unauthenticated rate limit (10/min for Search) <em>per server IP, across every
 * user</em> - a second person analyzing shortly after another already exhausts it. {@code
 * serverToken} (a PAT with no special scopes, set via {@code GITHUB_API_TOKEN}) is used as a
 * fallback so real requests get the authenticated limits (30/min Search, 5000/hr core) instead.
 */
@Component
public class GithubApiClient {

    private static final int PER_PAGE = 100;
    // GitHub search caps results at 1000 (10 pages), but every extra page is another 2s-spaced
    // request against the secondary rate limit shared across all concurrent users - capped lower
    // to keep one analysis fast and the single-threaded queue (AsyncConfig) moving. Results come
    // back newest-first, so this is "your most recent ~400 commits", not a random sample - most
    // accounts never hit 400 commits at all and see no difference; the ratio-based scores
    // (night-owl/bug-fix/refactor ratios etc.) barely move either way. Only raw commitVolumeNorm
    // and very prolific committers' full history are affected.
    private static final int MAX_SEARCH_PAGES = 4;

    private final RestClient restClient;
    private final String serverToken;

    public GithubApiClient(RestClient.Builder builder, @Value("${app.github.token:}") String serverToken) {
        this.restClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("User-Agent", "DevDNA")
                .build();
        this.serverToken = serverToken;
    }

    public List<GithubRepoItem> fetchOwnedRepositories(String username, String accessToken) {
        List<GithubRepoItem> all = new ArrayList<>();
        int page = 1;
        while (true) {
            List<GithubRepoItem> pageItems = get(
                    "/users/{username}/repos?type=owner&per_page={perPage}&page={page}",
                    new ParameterizedTypeReference<List<GithubRepoItem>>() {},
                    accessToken, username, PER_PAGE, page);
            if (pageItems == null || pageItems.isEmpty()) {
                break;
            }
            all.addAll(pageItems);
            if (pageItems.size() < PER_PAGE) {
                break;
            }
            page++;
        }
        return all.stream().filter(r -> r.fork() == null || !r.fork()).toList();
    }

    public List<GithubCommitItem> searchCommits(String username, String accessToken) {
        List<GithubCommitItem> all = new ArrayList<>();
        for (int page = 1; page <= MAX_SEARCH_PAGES; page++) {
            // Firing all ~10 pages back-to-back trips GitHub's Search API *secondary* rate limit
            // (abuse detection, separate from and much stricter than the per-minute quota) even
            // with an authenticated token - it wants roughly a second between search requests.
            if (page > 1) {
                sleepBetweenSearchPages();
            }
            GithubSearchResponse<GithubCommitItem> response = get(
                    "/search/commits?q=author:{username}&sort=author-date&order=desc&per_page={perPage}&page={page}",
                    new ParameterizedTypeReference<GithubSearchResponse<GithubCommitItem>>() {},
                    accessToken, username, PER_PAGE, page);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                break;
            }
            all.addAll(response.items());
            if (response.items().size() < PER_PAGE) {
                break;
            }
        }
        return all;
    }

    private void sleepBetweenSearchPages() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public int countPullRequests(String username, String accessToken) {
        GithubSearchResponse<Object> response = get(
                "/search/issues?q=author:{username}+type:pr&per_page=1",
                new ParameterizedTypeReference<GithubSearchResponse<Object>>() {},
                accessToken, username);
        return response == null || response.totalCount() == null ? 0 : response.totalCount();
    }

    private <T> T get(String uri, ParameterizedTypeReference<T> type, String accessToken, Object... uriVars) {
        String bearer = accessToken != null && !accessToken.isBlank() ? accessToken : serverToken;
        try {
            return restClient.get()
                    .uri(uri, uriVars)
                    .headers(h -> {
                        if (bearer != null && !bearer.isBlank()) {
                            h.setBearerAuth(bearer);
                        }
                    })
                    .retrieve()
                    .body(type);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw toGithubApiException(e);
        }
    }

    private GithubApiException toGithubApiException(org.springframework.web.client.HttpStatusCodeException e) {
        HttpStatusCode status = e.getStatusCode();
        HttpHeaders headers = e.getResponseHeaders();
        boolean primaryRateLimited = status.value() == 403
                && headers != null
                && "0".equals(headers.getFirst("x-ratelimit-remaining"));
        // Secondary (abuse-detection) rate limit: same 403 status, but x-ratelimit-remaining is
        // untouched - GitHub signals it in the body instead. See sleepBetweenSearchPages, which
        // exists to avoid tripping this in the first place.
        boolean secondaryRateLimited = status.value() == 403
                && e.getResponseBodyAsString().toLowerCase().contains("secondary rate limit");
        boolean retryable = primaryRateLimited || secondaryRateLimited || status.value() == 429
                || status.is5xxServerError();
        String message = "GitHub API error " + status.value()
                + (primaryRateLimited || secondaryRateLimited ? " (rate limited)" : "");
        return new GithubApiException(message, retryable);
    }
}
