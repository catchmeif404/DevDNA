package dev.devwrapped.backend.analyzer;

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
        int repositoryDiversity,
        dev.devwrapped.backend.collector.Observation observation) {
    public Features(int commitCount, int repositoryCount, int pullRequestCount,
            Map<String, Long> languageCounts, Map<String, Double> languageRatios, String topLanguage,
            int peakHour, int peakWeekday, double nightCommitRatio, double weekendCommitRatio,
            double featureRatio, double fixRatio, double refactorRatio, double documentationRatio,
            double testRatio, int distinctActiveDays, int repositoryDiversity) {
        this(commitCount, repositoryCount, pullRequestCount, languageCounts, languageRatios, topLanguage,
                peakHour, peakWeekday, nightCommitRatio, weekendCommitRatio, featureRatio, fixRatio,
                refactorRatio, documentationRatio, testRatio, distinctActiveDays, repositoryDiversity,
                dev.devwrapped.backend.collector.Observation.empty());
    }
}
