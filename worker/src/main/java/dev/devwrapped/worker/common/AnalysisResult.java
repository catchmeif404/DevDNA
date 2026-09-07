package dev.devwrapped.worker.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mirrors backend's AnalysisResult (same table). */
@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_job_id", nullable = false)
    private Long analysisJobId;

    @Column(name = "github_username", nullable = false)
    private String githubUsername;

    @Column(name = "total_commits", nullable = false)
    private Integer totalCommits;

    @Column(name = "total_repositories", nullable = false)
    private Integer totalRepositories;

    @Column(name = "total_pull_requests", nullable = false)
    private Integer totalPullRequests;

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "peak_weekday")
    private Integer peakWeekday;

    @Column(name = "top_language")
    private String topLanguage;

    @Column(name = "language_ratios")
    private String languageRatios;

    @Column(name = "night_owl_score", nullable = false)
    private Integer nightOwlScore;

    @Column(name = "bug_slayer_score", nullable = false)
    private Integer bugSlayerScore;

    @Column(name = "builder_score", nullable = false)
    private Integer builderScore;

    @Column(name = "polyglot_score", nullable = false)
    private Integer polyglotScore;

    @Column(name = "developer_type", nullable = false)
    private String developerType;

    @Column(name = "dna_vector")
    private String dnaVector;

    @Column(name = "ai_summary")
    private String aiSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
