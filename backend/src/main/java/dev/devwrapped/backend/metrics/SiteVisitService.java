package dev.devwrapped.backend.metrics;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteVisitService {

    private final SiteVisitEventRepository siteVisitEventRepository;

    @Transactional
    public void recordVisit(String hostname, String visitorId) {
        siteVisitEventRepository.save(new SiteVisitEvent(hostname.trim(), normalizeVisitorId(visitorId), Instant.now()));
    }

    private String normalizeVisitorId(String visitorId) {
        return visitorId == null || visitorId.isBlank() ? null : visitorId.trim();
    }
}
