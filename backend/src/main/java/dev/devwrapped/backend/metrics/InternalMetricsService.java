package dev.devwrapped.backend.metrics;

import dev.devwrapped.backend.metrics.dto.MetricsSummaryResponse;
import dev.devwrapped.backend.metrics.dto.ActiveUsersSummary;
import dev.devwrapped.backend.metrics.dto.DailyCount;
import dev.devwrapped.backend.metrics.dto.ViewsSummary;
import dev.devwrapped.backend.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalMetricsService {

    private static final int DAILY_WINDOW_DAYS = 30;

    private final UserRepository userRepository;
    private final SiteVisitEventRepository siteVisitEventRepository;

    @Transactional(readOnly = true)
    public MetricsSummaryResponse getSummary() {
        long memberCount = userRepository.count();
        Instant since = Instant.now().minus(DAILY_WINDOW_DAYS, ChronoUnit.DAYS);
        var daily = siteVisitEventRepository.countDailySince(since).stream()
                .map(row -> new DailyCount(row.getDay(), row.getCount()))
                .toList();
        var views = new ViewsSummary(siteVisitEventRepository.count(), daily);
        var activeUsers = new ActiveUsersSummary(
                siteVisitEventRepository.countDistinctVisitorsOnDate(LocalDate.now()),
                siteVisitEventRepository.countDistinctVisitorsSince(since));
        return new MetricsSummaryResponse(memberCount, views, activeUsers);
    }
}
