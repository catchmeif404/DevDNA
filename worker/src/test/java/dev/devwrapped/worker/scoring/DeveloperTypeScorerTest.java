package dev.devwrapped.worker.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.worker.analyzer.Features;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeveloperTypeScorerTest {

    private final DeveloperTypeScorer scorer = new DeveloperTypeScorer();

    @Test
    void picksNightOwlWhenNightRatioDominates() {
        Features features = features(0.6, 0.1, Map.of("Java", 1L), 100, 30, 5);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("NIGHT_OWL");
        assertThat(result.nightOwlScore()).isEqualTo(60);
    }

    @Test
    void picksPolyglotWhenManyLanguages() {
        Features features = features(0.1, 0.1, Map.of(
                "Java", 1L, "Kotlin", 1L, "TypeScript", 1L, "Python", 1L,
                "Go", 1L, "Rust", 1L, "C++", 1L, "Ruby", 1L), 50, 10, 5);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("POLYGLOT");
        assertThat(result.polyglotScore()).isEqualTo(100);
    }

    @Test
    void dnaVectorHasTenDimensionsClampedToUnitRange() {
        Features features = features(0.9, 0.9, Map.of("Java", 1L), 5000, 400, 100);
        ScoringResult result = scorer.score(features);
        assertThat(result.dnaVector()).hasSize(10);
        assertThat(result.dnaVector()).allSatisfy(v -> assertThat(v).isBetween(0.0, 1.0));
    }

    private Features features(double nightRatio, double weekendRatio, Map<String, Long> languageCounts,
            int commitCount, int distinctActiveDays, int repositoryCount) {
        return new Features(
                commitCount,
                repositoryCount,
                20,
                languageCounts,
                Map.of(),
                languageCounts.keySet().stream().findFirst().orElse(null),
                2,
                3,
                nightRatio,
                weekendRatio,
                0.2,
                0.1,
                0.1,
                0.05,
                0.05,
                distinctActiveDays,
                repositoryCount);
    }
}
