package dev.devwrapped.backend.analysis;

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

    /** JSON-serialized {@code Map<String, Double>} of language -> ratio. */
    @Column(name = "language_ratios")
    private String languageRatios;

    /** JSON-serialized {@code Map<String, Integer>} of developer type -> score (0-100). */
    @Column(name = "type_scores")
    private String typeScores;

    @Column(name = "developer_type", nullable = false)
    private String developerType;

    /** JSON-serialized 10-dimension normalized feature vector (section 13). */
    @Column(name = "dna_vector")
    private String dnaVector;

    @Column(name = "ai_summary")
    private String aiSummary;

    /** 관찰된 커밋의 가장 오래된 작성 시각이다. */
    @Column(name = "observation_from", nullable = false)
    private String observationFrom;

    /** 관찰된 커밋의 가장 최근 작성 시각이다. */
    @Column(name = "observation_to", nullable = false)
    private String observationTo;

    /** 최근 커밋 샘플 상한에 도달했는지 나타낸다. */
    @Column(name = "commit_sample_capped", nullable = false)
    private boolean commitSampleCapped;

    /** 관찰 범위의 제한사항을 JSON 배열로 저장한다. */
    @Column(name = "observation_limitations", nullable = false)
    private String observationLimitations;

    /** 분류 근거의 충분성을 나타내는 상태다. */
    @Column(name = "classification_status", nullable = false)
    private String classificationStatus;

    /** 1위와 2위 유형 점수의 차이다. */
    @Column(name = "classification_margin", nullable = false)
    private Integer classificationMargin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
