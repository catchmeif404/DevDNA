package dev.devwrapped.worker.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubSearchResponse<T>(Integer totalCount, List<T> items) {
}
