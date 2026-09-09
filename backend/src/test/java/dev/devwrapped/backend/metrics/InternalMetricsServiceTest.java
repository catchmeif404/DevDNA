package dev.devwrapped.backend.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.devwrapped.backend.metrics.dto.MetricsSummaryResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import dev.devwrapped.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalMetricsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SiteVisitEventRepository siteVisitEventRepository;

    @Test
    void returnsMemberAndVisitMetrics() {
        when(userRepository.count()).thenReturn(7L);
        when(siteVisitEventRepository.count()).thenReturn(12L);
        when(siteVisitEventRepository.countDailySince(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of());
        when(siteVisitEventRepository.countDistinctVisitorsOnDate(LocalDate.now())).thenReturn(2L);
        when(siteVisitEventRepository.countDistinctVisitorsSince(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(5L);
        InternalMetricsService service = new InternalMetricsService(userRepository, siteVisitEventRepository);

        MetricsSummaryResponse summary = service.getSummary();

        assertThat(summary.memberCount()).isEqualTo(7L);
        assertThat(summary.views().total()).isEqualTo(12L);
        assertThat(summary.activeUsers().dau()).isEqualTo(2L);
        assertThat(summary.activeUsers().mau()).isEqualTo(5L);
    }
}
