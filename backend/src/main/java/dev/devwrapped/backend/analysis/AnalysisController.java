package dev.devwrapped.backend.analysis;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/api/analyses")
    public ResponseEntity<?> create(@Valid @RequestBody CreateAnalysisRequest request) {
        AnalysisJob job = new AnalysisJob(request.githubUsername());
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
