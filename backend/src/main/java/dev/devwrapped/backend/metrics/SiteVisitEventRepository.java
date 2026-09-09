package dev.devwrapped.backend.metrics;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteVisitEventRepository extends JpaRepository<SiteVisitEvent, Long> {

    @Query(value = "SELECT CAST(visited_at AS date) AS day, COUNT(*) AS count "
            + "FROM site_visit_events WHERE visited_at >= :since "
            + "GROUP BY CAST(visited_at AS date) ORDER BY day", nativeQuery = true)
    List<DailyVisitCount> countDailySince(@Param("since") Instant since);

    @Query(value = "SELECT COUNT(DISTINCT visitor_id) FROM site_visit_events "
            + "WHERE visitor_id IS NOT NULL AND CAST(visited_at AS date) = :date", nativeQuery = true)
    long countDistinctVisitorsOnDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(DISTINCT visitor_id) FROM site_visit_events "
            + "WHERE visitor_id IS NOT NULL AND visited_at >= :since", nativeQuery = true)
    long countDistinctVisitorsSince(@Param("since") Instant since);
}
