package dev.devwrapped.backend.scoring;

import java.util.List;
import java.util.Map;

public record ScoringResult(
        Map<String, Integer> typeScores,
        String developerType,
        List<Double> dnaVector) {
}
