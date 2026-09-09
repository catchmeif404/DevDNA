package dev.devwrapped.backend.scoring;

import dev.devwrapped.backend.analyzer.Features;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Rule-based scoring (section 12/13) — no LLM involved.
 *
 * <p>10 developer types, each keyed to a single normalized feature so no type wins just because
 * its denominator is easy to max out. Ties keep the first type listed in {@code scoreTypes}.
 *
 * <p><b>The denominators below matter a lot — they were miscalibrated once already</b> (see
 * {@code fewLanguagesScoreLowerOnPolyglotThanBefore}'s regression comment: POLYGLOT used to be
 * {@code / 8} and dominated for the same structural reason described next). EXPLORER's {@code
 * repositoryDiversity / 15} had the same bug: a moderately active account touching just 4 repos
 * (very common — personal projects, a couple of org repos, one fork) already scored 27%, while
 * the commit-ratio types (NIGHT_OWL, BUG_SLAYER, etc.) split their numerator across 6 commit-type
 * buckets and rarely clear 30% for a typical user. Confirmed empirically: three different real
 * accounts all came back EXPLORER TURTLE. Raised so "explorer" requires genuinely unusual
 * repository breadth, not just having more than a couple of repos.
 */
@Service
public class DeveloperTypeScorer {

    private static final int MIN_COMMIT_EVIDENCE = 10;
    private static final int UNCERTAIN_MARGIN = 8;

    public ScoringResult score(Features f) {
        double persistenceNorm = Math.min(1.0, f.distinctActiveDays() / 180.0);
        double languageDiversityNorm = Math.min(1.0, f.languageCounts().size() / 15.0);
        double repositoryDiversityNorm = Math.min(1.0, f.repositoryDiversity() / 30.0);
        double prActivityNorm = Math.min(1.0, f.pullRequestCount() / 20.0);

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("NIGHT_OWL", pct(f.nightCommitRatio()));
        scores.put("BUG_SLAYER", pct(f.fixRatio()));
        scores.put("BUILDER", pct(f.featureRatio() * 0.6 + persistenceNorm * 0.4));
        scores.put("POLYGLOT", pct(languageDiversityNorm));
        scores.put("WEEKEND_WARRIOR", pct(f.weekendCommitRatio()));
        scores.put("REFACTOR_MASTER", pct(f.refactorRatio()));
        scores.put("DOCUMENTARIAN", pct(f.documentationRatio()));
        scores.put("TESTER", pct(f.testRatio()));
        scores.put("EXPLORER", pct(repositoryDiversityNorm));
        scores.put("COLLABORATOR", pct(prActivityNorm));

        String topType = scores.entrySet().stream()
                .reduce((a, b) -> b.getValue() > a.getValue() ? b : a)
                .map(Map.Entry::getKey)
                .orElse("BUILDER");
        List<Integer> rankedScores = scores.values().stream().sorted((a, b) -> Integer.compare(b, a)).toList();
        int margin = rankedScores.size() < 2 ? 0 : rankedScores.get(0) - rankedScores.get(1);
        String classificationStatus = f.commitCount() < MIN_COMMIT_EVIDENCE
                ? "INSUFFICIENT_EVIDENCE"
                : margin < UNCERTAIN_MARGIN ? "UNCERTAIN" : "CLASSIFIED";
        String developerType = f.commitCount() < MIN_COMMIT_EVIDENCE ? "UNCLASSIFIED" : topType;

        double commitVolumeNorm = Math.min(1.0, f.commitCount() / 1000.0);

        List<Double> dnaVector = List.of(
                round2(commitVolumeNorm),
                round2(f.featureRatio()),
                round2(f.fixRatio()),
                round2(f.refactorRatio()),
                round2(languageDiversityNorm),
                round2(f.nightCommitRatio()),
                round2(f.weekendCommitRatio()),
                round2(persistenceNorm),
                round2(prActivityNorm),
                round2(repositoryDiversityNorm));

        return new ScoringResult(scores, developerType, dnaVector, classificationStatus, margin);
    }

    private int pct(double ratio) {
        return Math.max(0, Math.min(100, (int) Math.round(ratio * 100)));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
