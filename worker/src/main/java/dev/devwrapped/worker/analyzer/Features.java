package dev.devwrapped.worker.analyzer;

import java.util.Map;

public record Features(
        int commitCount,
        int repositoryCount,
        int pullRequestCount,
        Map<String, Long> languageCounts,
        Map<String, Double> languageRatios,
        String topLanguage,
        int peakHour,
        int peakWeekday,
        double nightCommitRatio,
        double weekendCommitRatio,
        double featureRatio,
        double fixRatio,
        double refactorRatio,
        double documentationRatio,
        double testRatio,
        int distinctActiveDays,
        int repositoryDiversity) {
}
