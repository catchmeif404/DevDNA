package dev.devwrapped.backend.collector;

import java.util.List;
import java.util.Map;

/** 수집 범위와 누락 여부를 결과와 함께 보존한다. */
public record Observation(String from, String to, boolean commitSampleCapped,
        int externalPullRequests, int reviewedPullRequests, int fileSampleCount,
        int documentationCommits, int testCommits, Map<String, Long> languages,
        List<String> limitations) {
    public static Observation empty() {
        return new Observation("", "", false, 0, 0, 0, 0, 0, Map.of(), List.of());
    }
}
