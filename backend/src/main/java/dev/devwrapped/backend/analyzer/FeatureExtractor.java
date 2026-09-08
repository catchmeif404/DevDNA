package dev.devwrapped.backend.analyzer;

import dev.devwrapped.backend.collector.GithubCommitItem;
import dev.devwrapped.backend.collector.GithubRepoItem;
import dev.devwrapped.backend.collector.RawActivity;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Deterministic feature extraction (section 9/14) — commit-type classification is the one step
 * that may consult an LLM ({@link CommitTypeResolver}), but everything downstream of it (ratios,
 * histograms, the returned {@link Features} record) is still plain arithmetic over a fixed list of
 * labels, not a number an AI was asked to produce directly.
 */
@Component
public class FeatureExtractor {

    private final CommitTypeResolver commitTypeResolver;

    public FeatureExtractor(CommitTypeResolver commitTypeResolver) {
        this.commitTypeResolver = commitTypeResolver;
    }

    public Features extract(RawActivity raw) {
        Map<String, Long> languageCounts = raw.repositories().stream()
                .map(GithubRepoItem::language)
                .filter(lang -> lang != null && !lang.isBlank())
                .collect(Collectors.groupingBy(lang -> lang, Collectors.counting()));
        long languageTotal = languageCounts.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> languageRatios = new HashMap<>();
        languageCounts.forEach((lang, count) -> languageRatios.put(lang, languageTotal == 0 ? 0.0 : (double) count / languageTotal));
        String topLanguage = languageCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        int[] hourHistogram = new int[24];
        int[] weekdayHistogram = new int[7];
        int nightCommits = 0;
        int weekendCommits = 0;
        Map<CommitType, Integer> typeCounts = new HashMap<>();
        Set<LocalDate> activeDays = new HashSet<>();
        Set<String> activeRepos = new HashSet<>();

        List<String> messages = raw.commits().stream()
                .map(commit -> commit.commit() == null ? null : commit.commit().message())
                .toList();
        List<CommitType> types = commitTypeResolver.resolveAll(messages);

        List<GithubCommitItem> commits = raw.commits();
        for (int i = 0; i < commits.size(); i++) {
            GithubCommitItem commit = commits.get(i);
            String isoDate = commit.commit() == null || commit.commit().author() == null
                    ? null
                    : commit.commit().author().date();
            if (isoDate != null) {
                OffsetDateTime dateTime = OffsetDateTime.parse(isoDate);
                int hour = dateTime.getHour();
                DayOfWeek weekday = dateTime.getDayOfWeek();
                hourHistogram[hour]++;
                weekdayHistogram[weekday.getValue() - 1]++;
                activeDays.add(dateTime.toLocalDate());
                if (hour >= 0 && hour < 6) {
                    nightCommits++;
                }
                if (weekday == DayOfWeek.SATURDAY || weekday == DayOfWeek.SUNDAY) {
                    weekendCommits++;
                }
            }
            if (commit.repository() != null && commit.repository().fullName() != null) {
                activeRepos.add(commit.repository().fullName());
            }
            typeCounts.merge(types.get(i), 1, Integer::sum);
        }

        int commitCount = raw.commits().size();
        int peakHour = indexOfMax(hourHistogram);
        int peakWeekday = indexOfMax(weekdayHistogram) + 1; // 1=Monday .. 7=Sunday

        return new Features(
                commitCount,
                raw.repositories().size(),
                raw.pullRequestCount(),
                languageCounts,
                languageRatios,
                topLanguage,
                peakHour,
                peakWeekday,
                ratio(nightCommits, commitCount),
                ratio(weekendCommits, commitCount),
                ratio(typeCounts.getOrDefault(CommitType.FEATURE, 0), commitCount),
                ratio(typeCounts.getOrDefault(CommitType.FIX, 0), commitCount),
                ratio(typeCounts.getOrDefault(CommitType.REFACTOR, 0), commitCount),
                ratio(typeCounts.getOrDefault(CommitType.DOCUMENTATION, 0), commitCount),
                ratio(typeCounts.getOrDefault(CommitType.TEST, 0), commitCount),
                activeDays.size(),
                activeRepos.size());
    }

    private double ratio(int part, int total) {
        return total == 0 ? 0.0 : (double) part / total;
    }

    private int indexOfMax(int[] histogram) {
        int maxIndex = 0;
        for (int i = 1; i < histogram.length; i++) {
            if (histogram[i] > histogram[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
