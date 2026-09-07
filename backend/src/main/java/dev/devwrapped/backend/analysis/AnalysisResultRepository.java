package dev.devwrapped.backend.analysis;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findByAnalysisJobId(Long analysisJobId);

    Optional<AnalysisResult> findTopByGithubUsernameOrderByCreatedAtDesc(String githubUsername);
}
