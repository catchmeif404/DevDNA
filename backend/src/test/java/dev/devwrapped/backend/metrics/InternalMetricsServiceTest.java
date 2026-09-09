package dev.devwrapped.backend.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.devwrapped.backend.metrics.dto.MetricsSummaryResponse;
import dev.devwrapped.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalMetricsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void returnsMemberCountOnlyUntilVisitTrackingExists() {
        when(userRepository.count()).thenReturn(7L);
        InternalMetricsService service = new InternalMetricsService(userRepository);

        MetricsSummaryResponse summary = service.getSummary();

        assertThat(summary.memberCount()).isEqualTo(7L);
        assertThat(summary.views()).isNull();
        assertThat(summary.activeUsers()).isNull();
    }
}
