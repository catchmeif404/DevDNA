package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import dev.devwrapped.backend.analysis.AnalysisResultRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShareController {

    private final AnalysisResultRepository resultRepository;
    private final ShareCardGenerator cardGenerator;
    private final BadgeGenerator badgeGenerator;

    public ShareController(AnalysisResultRepository resultRepository, ShareCardGenerator cardGenerator,
            BadgeGenerator badgeGenerator) {
        this.resultRepository = resultRepository;
        this.cardGenerator = cardGenerator;
        this.badgeGenerator = badgeGenerator;
    }

    @GetMapping("/api/share/{username}")
    public ResponseEntity<?> shareData(@PathVariable String username) {
        return resultRepository.findTopByGithubUsernameOrderByCreatedAtDesc(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/api/share/{username}/card", produces = "image/svg+xml")
    public ResponseEntity<String> shareCard(@PathVariable String username) {
        AnalysisResult result = resultRepository.findTopByGithubUsernameOrderByCreatedAtDesc(username).orElse(null);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(cardGenerator.generate(result));
    }

    /** Public README badge (section 17) — always returns 200 so embedding it never shows a broken image. */
    @GetMapping(value = "/api/badge/{username:[A-Za-z0-9-]+}.svg", produces = "image/svg+xml")
    public ResponseEntity<String> badge(@PathVariable String username) {
        AnalysisResult result = resultRepository.findTopByGithubUsernameOrderByCreatedAtDesc(username).orElse(null);
        String svg = result == null ? badgeGenerator.generateNoData() : badgeGenerator.generateForResult(result);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .cacheControl(CacheControl.noCache())
                .body(svg);
    }
}
