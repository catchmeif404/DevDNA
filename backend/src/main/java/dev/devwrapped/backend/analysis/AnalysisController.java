package dev.devwrapped.backend.analysis;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    private final AnalysisJobRepository jobRepository;
    private final AnalysisResultRepository resultRepository;
    private final AnalysisQueuePublisher queuePublisher;

    public AnalysisController(AnalysisJobRepository jobRepository, AnalysisResultRepository resultRepository,
            AnalysisQueuePublisher queuePublisher) {
        this.jobRepository = jobRepository;
        this.resultRepository = resultRepository;
        this.queuePublisher = queuePublisher;
    }

    /** Always analyzes the caller's own GitHub account — SecurityConfig requires authentication here. */
    @PostMapping("/api/analyses")
    public ResponseEntity<?> create(@AuthenticationPrincipal OAuth2User principal) {
        String githubUsername = principal.getAttribute("login");
        AnalysisJob job = new AnalysisJob(githubUsername);
        job = jobRepository.save(job);
        queuePublisher.enqueue(job.getId());
        return ResponseEntity.ok(Map.of("jobId", job.getId(), "status", job.getStatus()));
    }

    @GetMapping("/api/analyses/{jobId}")
    public ResponseEntity<?> status(@PathVariable Long jobId) {
        return jobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(Map.of(
                        "jobId", job.getId(),
                        "status", job.getStatus(),
                        "progress", job.getProgress(),
                        "errorMessage", job.getErrorMessage() == null ? "" : job.getErrorMessage())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/analyses/{jobId}/result")
    public ResponseEntity<?> result(@PathVariable Long jobId) {
        return resultRepository.findByAnalysisJobId(jobId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
