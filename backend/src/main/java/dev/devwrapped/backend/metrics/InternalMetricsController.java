package dev.devwrapped.backend.metrics;

import dev.devwrapped.backend.metrics.dto.MetricsSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/metrics")
@RequiredArgsConstructor
public class InternalMetricsController {

    private final InternalMetricsService internalMetricsService;

    @GetMapping("/summary")
    public MetricsSummaryResponse summary() {
        return internalMetricsService.getSummary();
    }
}
