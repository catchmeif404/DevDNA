package dev.devwrapped.backend.collector;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin GitHub REST API client. Uses the Search API for commits/PRs instead of iterating every
 * repository (section 9) — far fewer requests, and reflects public activity across all of GitHub,
 * not just the user's own repos.
 */
@Component
public class GithubApiClient {

    private static final int PER_PAGE = 100;
    private static final int MAX_SEARCH_PAGES = 10; // GitHub search caps results at 1000

    private final RestClient restClient;

    public GithubApiClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("User-Agent", "DevDNA")
                .build();
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

    public int countPullRequests(String username, String accessToken) {
        GithubSearchResponse<Object> response = get(
                "/search/issues?q=author:{username}+type:pr&per_page=1",
                new ParameterizedTypeReference<GithubSearchResponse<Object>>() {},
                accessToken, username);
        return response == null || response.totalCount() == null ? 0 : response.totalCount();
    }

    private <T> T get(String uri, ParameterizedTypeReference<T> type, String accessToken, Object... uriVars) {
        try {
            return restClient.get()
                    .uri(uri, uriVars)
                    .headers(h -> {
                        if (accessToken != null && !accessToken.isBlank()) {
                            h.setBearerAuth(accessToken);
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
        boolean rateLimited = status.value() == 403
                && headers != null
                && "0".equals(headers.getFirst("x-ratelimit-remaining"));
        boolean retryable = rateLimited || status.value() == 429 || status.is5xxServerError();
        String message = "GitHub API error " + status.value() + (rateLimited ? " (rate limited)" : "");
        return new GithubApiException(message, retryable);
    }
}
