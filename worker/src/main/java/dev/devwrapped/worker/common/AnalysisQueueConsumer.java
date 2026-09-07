package dev.devwrapped.worker.common;

import tools.jackson.databind.ObjectMapper;
import dev.devwrapped.worker.analyzer.FeatureExtractor;
import dev.devwrapped.worker.analyzer.Features;
import dev.devwrapped.worker.ai.SummaryGenerator;
import dev.devwrapped.worker.collector.CollectorService;
import dev.devwrapped.worker.collector.RawActivity;
import dev.devwrapped.worker.scoring.DeveloperTypeScorer;
import dev.devwrapped.worker.scoring.ScoringResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Pops job ids from the Redis-backed queue and runs collect -> analyze -> score -> persist. */
@Component
public class AnalysisQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalysisQueueConsumer.class);

    private final StringRedisTemplate redisTemplate;
    private final AnalysisJobRepository jobRepository;
    private final AnalysisResultRepository resultRepository;
    private final CollectorService collectorService;
    private final FeatureExtractor featureExtractor;
    private final DeveloperTypeScorer scorer;
    private final SummaryGenerator summaryGenerator;
    private final ObjectMapper objectMapper;
    private final String queueKey;
    private final String dlqKey;

    private ExecutorService executor;
    private volatile boolean running = true;

    public AnalysisQueueConsumer(StringRedisTemplate redisTemplate, AnalysisJobRepository jobRepository,
            AnalysisResultRepository resultRepository, CollectorService collectorService,
            FeatureExtractor featureExtractor, DeveloperTypeScorer scorer, SummaryGenerator summaryGenerator,
            ObjectMapper objectMapper,
            @Value("${app.analysis-queue-key}") String queueKey,
            @Value("${app.analysis-dlq-key}") String dlqKey) {
        this.redisTemplate = redisTemplate;
        this.jobRepository = jobRepository;
        this.resultRepository = resultRepository;
        this.collectorService = collectorService;
        this.featureExtractor = featureExtractor;
        this.scorer = scorer;
        this.summaryGenerator = summaryGenerator;
        this.objectMapper = objectMapper;
        this.queueKey = queueKey;
        this.dlqKey = dlqKey;
    }

    @PostConstruct
    void start() {
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "analysis-queue-consumer"));
        executor.submit(this::loop);
    }

    @PreDestroy
    void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void loop() {
        while (running) {
            try {
                String jobIdRaw = redisTemplate.opsForList().leftPop(queueKey, Duration.ofSeconds(5));
                if (jobIdRaw == null) {
                    continue;
                }
                processJob(Long.valueOf(jobIdRaw));
            } catch (Exception e) {
                log.error("Unexpected error in analysis queue loop", e);
            }
        }
    }

    private void processJob(Long jobId) {
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
            redisTemplate.opsForList().rightPush(dlqKey, String.valueOf(jobId));
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
