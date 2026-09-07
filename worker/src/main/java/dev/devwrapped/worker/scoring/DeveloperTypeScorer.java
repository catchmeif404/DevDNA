package dev.devwrapped.worker.scoring;

import dev.devwrapped.worker.analyzer.Features;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Rule-based scoring (section 12/13) — no LLM involved. */
@Service
public class DeveloperTypeScorer {

    public ScoringResult score(Features f) {
        int nightOwl = clamp(round(f.nightCommitRatio() * 100));
        int bugSlayer = clamp(round(f.fixRatio() * 100));
        double persistenceNorm = Math.min(1.0, f.distinctActiveDays() / 180.0);
        int builder = clamp(round((f.featureRatio() * 0.6 + persistenceNorm * 0.4) * 100));
        double languageDiversityNorm = Math.min(1.0, f.languageCounts().size() / 8.0);
        int polyglot = clamp(round(languageDiversityNorm * 100));

        Map<String, Integer> scores = Map.of(
                "NIGHT_OWL", nightOwl,
                "BUG_SLAYER", bugSlayer,
                "BUILDER", builder,
                "POLYGLOT", polyglot);
        String developerType = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("BUILDER");

        double commitVolumeNorm = Math.min(1.0, f.commitCount() / 1000.0);
        double repositoryDiversityNorm = Math.min(1.0, f.repositoryCount() / 20.0);
        double prActivityNorm = Math.min(1.0, f.pullRequestCount() / 50.0);

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

        return new ScoringResult(nightOwl, bugSlayer, builder, polyglot, developerType, dnaVector);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int round(double value) {
        return (int) Math.round(value);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
