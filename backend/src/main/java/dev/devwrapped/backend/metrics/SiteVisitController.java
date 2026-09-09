package dev.devwrapped.backend.metrics;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SiteVisitController {

    private final SiteVisitService siteVisitService;

    @PostMapping("/api/site-visits/ping")
    public ResponseEntity<Void> ping(@Valid @RequestBody SiteVisitPingRequest request) {
        siteVisitService.recordVisit(request.hostname(), request.visitorId());
        return ResponseEntity.noContent().build();
    }

    public record SiteVisitPingRequest(
            @NotBlank @Size(max = 255) String hostname,
            @Size(max = 64) String visitorId
    ) {
    }
}
