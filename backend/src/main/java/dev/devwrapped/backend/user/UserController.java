package dev.devwrapped.backend.user;

import dev.devwrapped.backend.analysis.AnalysisResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final AnalysisResultRepository resultRepository;

    public UserController(AnalysisResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(java.util.Map.of(
                "githubLogin", principal.getAttribute("login"),
                "avatarUrl", principal.getAttribute("avatar_url")));
    }

    @GetMapping("/api/users/{username}/result")
    public ResponseEntity<?> latestResult(@PathVariable String username) {
        return resultRepository.findTopByGithubUsernameOrderByCreatedAtDesc(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
