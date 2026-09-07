package dev.devwrapped.worker.scoring;

import java.util.List;

public record ScoringResult(
        int nightOwlScore,
        int bugSlayerScore,
        int builderScore,
        int polyglotScore,
        String developerType,
        List<Double> dnaVector) {
}
