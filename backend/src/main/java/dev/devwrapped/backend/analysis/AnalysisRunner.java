package dev.devwrapped.backend.analysis;

import dev.devwrapped.backend.ai.SummaryGenerator;
import dev.devwrapped.backend.analyzer.FeatureExtractor;
import dev.devwrapped.backend.analyzer.Features;
import dev.devwrapped.backend.collector.CollectorService;
import dev.devwrapped.backend.collector.RawActivity;
import dev.devwrapped.backend.scoring.DeveloperTypeScorer;
import dev.devwrapped.backend.scoring.ScoringResult;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs collect -> extract -> score -> summarize -> persist for one job, off the calling thread.
 * Replaces the old Redis queue + separate worker service: at this traffic level (one self-analysis
 * per login) a single in-process async task does the same job with far less moving parts, and the
 * old queue's LPOP-without-visibility-timeout design already gave up an in-flight job on a crash
 * just like this does - so nothing durability-wise is actually lost. See docs/설계문서.md for the
 * original (deliberately over-built, portfolio-motivated) queue+worker design if reviving it.
 *
 * <p>One thing the old design got "for free" that this needed to restore deliberately:
 * {@code @Async("analysisExecutor")} runs on a single-thread executor (see AsyncConfig), not
 * Spring Boot's default pool of 8 - two analyses running concurrently would otherwise multiply
 * the burst of requests GithubApiClient sends against GitHub's shared-per-token secondary rate
 * limit. The old queue consumer serialized this automatically by only having one thread.
 */
@Component
public class AnalysisRunner {

    private static final Logger log = LoggerFactory.getLogger(AnalysisRunner.class);

    private final AnalysisJobRepository jobRepository;
    private final AnalysisResultRepository resultRepository;
    private final CollectorService collectorService;
    private final FeatureExtractor featureExtractor;
    private final DeveloperTypeScorer scorer;
    private final SummaryGenerator summaryGenerator;
    private final ObjectMapper objectMapper;

    public AnalysisRunner(AnalysisJobRepository jobRepository, AnalysisResultRepository resultRepository,
            CollectorService collectorService, FeatureExtractor featureExtractor, DeveloperTypeScorer scorer,
            SummaryGenerator summaryGenerator, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.resultRepository = resultRepository;
        this.collectorService = collectorService;
        this.featureExtractor = featureExtractor;
        this.scorer = scorer;
        this.summaryGenerator = summaryGenerator;
        this.objectMapper = objectMapper;
    }

    @Async("analysisExecutor")
    public void runAsync(Long jobId) {
        AnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Job {} not found, skipping", jobId);
            return;
        }
        try {
            job.setStatus(AnalysisStatus.COLLECTING);
            job.setProgress(10);
            job.setStartedAt(Instant.now());
            jobRepository.save(job);

            RawActivity raw = collectorService.collect(job.getGithubUsername(), null);

            job.setStatus(AnalysisStatus.ANALYZING);
            job.setProgress(60);
            jobRepository.save(job);

            Features features = featureExtractor.extract(raw);
            ScoringResult scoring = scorer.score(features);
            String summary = summaryGenerator.generate(features, scoring);

            AnalysisResult result = new AnalysisResult();
            result.setAnalysisJobId(job.getId());
            result.setGithubUsername(job.getGithubUsername());
            result.setTotalCommits(features.commitCount());
            result.setTotalRepositories(features.repositoryCount());
            result.setTotalPullRequests(features.pullRequestCount());
            result.setPeakHour(features.peakHour());
            result.setPeakWeekday(features.peakWeekday());
            result.setTopLanguage(features.topLanguage());
            result.setLanguageRatios(toJson(features.languageRatios()));
            result.setTypeScores(toJson(scoring.typeScores()));
            result.setDeveloperType(scoring.developerType());
            result.setDnaVector(toJson(scoring.dnaVector()));
            result.setAiSummary(summary);
            resultRepository.save(result);

            job.setStatus(AnalysisStatus.COMPLETED);
            job.setProgress(100);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        } catch (Exception e) {
            log.error("Analysis job {} failed", jobId, e);
            job.setStatus(AnalysisStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize {} to JSON", value, e);
            return null;
        }
    }
}
