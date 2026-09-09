package dev.devwrapped.backend.metrics;

import dev.devwrapped.backend.metrics.dto.MetricsSummaryResponse;
import dev.devwrapped.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalMetricsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MetricsSummaryResponse getSummary() {
        long memberCount = userRepository.count();
        return new MetricsSummaryResponse(memberCount, null, null);
    }
}
