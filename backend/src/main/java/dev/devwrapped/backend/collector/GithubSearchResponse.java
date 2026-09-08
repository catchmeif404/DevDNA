package dev.devwrapped.backend.collector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubSearchResponse<T>(@JsonProperty("total_count") Integer totalCount, List<T> items) {
}
