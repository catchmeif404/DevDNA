package dev.devwrapped.backend.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_visit_events")
@Getter
@NoArgsConstructor
public class SiteVisitEvent {

    // 방문 이벤트 식별자다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 방문이 발생한 호스트명이다.
    @Column(name = "hostname", nullable = false)
    private String hostname;

    // 로그인과 무관하게 브라우저에 저장되는 익명 방문자 식별자다.
    @Column(name = "visitor_id")
    private String visitorId;

    // 방문 시각이다.
    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    public SiteVisitEvent(String hostname, String visitorId, Instant visitedAt) {
        this.hostname = hostname;
        this.visitorId = visitorId;
        this.visitedAt = visitedAt;
    }
}
