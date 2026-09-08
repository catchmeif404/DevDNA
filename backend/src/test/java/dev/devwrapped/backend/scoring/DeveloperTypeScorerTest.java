package dev.devwrapped.backend.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.backend.analyzer.Features;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeveloperTypeScorerTest {

    private final DeveloperTypeScorer scorer = new DeveloperTypeScorer();

    @Test
    void picksNightOwlWhenNightRatioDominates() {
        Features features = features(0.6, 0.1, Map.of("Java", 1L), 100, 30, 5, 0);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("NIGHT_OWL");
        assertThat(result.typeScores().get("NIGHT_OWL")).isEqualTo(60);
    }

    @Test
    void picksPolyglotOnlyWhenLanguageDiversityIsActuallyHigh() {
        Features features = features(0.1, 0.1, Map.ofEntries(
                Map.entry("Java", 1L), Map.entry("Kotlin", 1L), Map.entry("TypeScript", 1L), Map.entry("Python", 1L),
                Map.entry("Go", 1L), Map.entry("Rust", 1L), Map.entry("C++", 1L), Map.entry("Ruby", 1L),
                Map.entry("Swift", 1L), Map.entry("C#", 1L), Map.entry("PHP", 1L), Map.entry("Scala", 1L),
                Map.entry("Elixir", 1L), Map.entry("Haskell", 1L), Map.entry("Dart", 1L)), 50, 10, 5, 0);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("POLYGLOT");
        assertThat(result.typeScores().get("POLYGLOT")).isEqualTo(100);
    }

    @Test
    void fewLanguagesScoreLowerOnPolyglotThanBefore() {
        // Regression: previously a /8 denominator gave 2 languages 25 pts, enough to auto-win
        // for most low-activity accounts. With a /15 denominator, 2 languages only scores 13.
        Features features = features(0.0, 0.0, Map.of("Java", 1L, "CSS", 1L), 5, 3, 4, 0);
        ScoringResult result = scorer.score(features);
        assertThat(result.typeScores().get("POLYGLOT")).isEqualTo(13);
    }

    @Test
    void picksExplorerOnlyWhenRepositoryDiversityIsActuallyHigh() {
        Features features = features(0.05, 0.05, Map.of("Java", 1L), 200, 30, 30, 0);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("EXPLORER");
        assertThat(result.typeScores().get("EXPLORER")).isEqualTo(100);
    }

    @Test
    void fewReposScoreLowerOnExplorerThanBefore() {
        // Regression: a /15 denominator gave 4 repos 27 pts, enough to auto-win almost every real
        // analysis (confirmed empirically — three different real accounts all came back EXPLORER
        // TURTLE). With a /30 denominator, 4 repos only scores 13.
        Features features = features(0.1, 0.1, Map.of("Java", 1L), 60, 20, 4, 0);
        ScoringResult result = scorer.score(features);
        assertThat(result.typeScores().get("EXPLORER")).isEqualTo(13);
    }

    @Test
    void picksCollaboratorWhenPullRequestActivityDominates() {
        Features features = features(0.0, 0.0, Map.of("Java", 1L), 10, 5, 3, 20);
        ScoringResult result = scorer.score(features);
        assertThat(result.developerType()).isEqualTo("COLLABORATOR");
        assertThat(result.typeScores().get("COLLABORATOR")).isEqualTo(100);
    }

    @Test
    void typeScoresCoverAllTenTypes() {
        Features features = features(0.1, 0.1, Map.of("Java", 1L), 50, 10, 5, 1);
        ScoringResult result = scorer.score(features);
        assertThat(result.typeScores()).hasSize(10);
    }

    @Test
    void dnaVectorHasTenDimensionsClampedToUnitRange() {
        Features features = features(0.9, 0.9, Map.of("Java", 1L), 5000, 400, 100, 50);
        ScoringResult result = scorer.score(features);
        assertThat(result.dnaVector()).hasSize(10);
        assertThat(result.dnaVector()).allSatisfy(v -> assertThat(v).isBetween(0.0, 1.0));
    }

    private Features features(double nightRatio, double weekendRatio, Map<String, Long> languageCounts,
            int commitCount, int distinctActiveDays, int repositoryCount, int pullRequestCount) {
        return new Features(
                commitCount,
                repositoryCount,
                pullRequestCount,
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
