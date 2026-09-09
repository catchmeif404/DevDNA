package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/** A static paper evidence label for GitHub README embeds. */
@Component
public class BadgeGenerator {
    public String generateForResult(AnalysisResult result, Integer founderRank) {
        return generateForResult(result, founderRank, false);
    }

    // Package-private sample mode is used only by the preview fixture, never by an endpoint.
    String generateForResult(AnalysisResult result, Integer founderRank, boolean sample) {
        if (result == null) {
            return generateNoData();
        }
        String subject = "@" + EvidenceSvg.clean(result.getGithubUsername(), "UNKNOWN", 39);
        String type = DeveloperTypeMeta.label(result.getDeveloperType());
        String stats = EvidenceSvg.count(result.getTotalCommits()) + " commits / "
                + EvidenceSvg.count(result.getTotalRepositories()) + " repos / "
                + EvidenceSvg.count(result.getTotalPullRequests()) + " PRs";
        return render(subject, type, stats,
                "LANGUAGE: " + EvidenceSvg.clean(result.getTopLanguage(), "UNRECORDED", 40),
                result.getDeveloperType(), result.getClassificationStatus(), founderRank, sample, false);
    }

    public String generateNoData() {
        return render("UNIDENTIFIED", "AWAITING EVIDENCE", "No public analysis on file.",
                "STATUS: no data yet", null, null, null, false, true);
    }

    private String render(String subject, String type, String stats, String language,
            String typeKey, String classificationStatus, Integer founderRank, boolean sample, boolean pending) {
        String founder = founderRank != null && founderRank > 0 ? "Founding member #" + founderRank : "";
        String status = sample ? "SAMPLE" : pending ? "PENDING" : statusLabel(classificationStatus);
        String title = "DevDNA: " + (sample ? "SAMPLE / " : "") + subject + " / " + type
                + (founder.isEmpty() ? "" : " / " + founder);
        return EvidenceSvg.open(600, 200, title) + """
                  <rect x="0.5" y="0.5" width="599" height="199" rx="16" fill="#f1e9d2" stroke="#d2c5aa"/>
                  <rect x="18" y="18" width="100" height="132" rx="12" fill="#e8dcc3"/>
                  <path d="M24 164 H576" fill="none" stroke="#241f1a" stroke-opacity="0.14"/>
                """
                + EvidenceSvg.text(36, 39, 12, 68, "DEVDNA")
                + EvidenceSvg.text(140, 37, 10, 260, "PUBLIC RECORD / " + status, "#a32b2b")
                + EvidenceSvg.typeMark(typeKey, 37, 57, 1.2)
                + EvidenceSvg.text(32, 136, 8, 76, "EVIDENCE LABEL")
                + EvidenceSvg.text(140, 65, 15, 436, subject)
                + EvidenceSvg.text(140, 100, 24, 436, type)
                + EvidenceSvg.text(140, 122, 9, 436, sample ? "SAMPLE SUBJECT" : "DEVELOPER PROFILE", "#756959")
                + EvidenceSvg.text(140, 148, 12, 436, stats)
                + EvidenceSvg.text(24, 182, 11, founder.isEmpty() ? 552 : 286, language)
                + (founder.isEmpty() ? "" : EvidenceSvg.text(326, 182, 11, 256, founder, "#a32b2b"))
                + "</svg>\n";
    }

    private String statusLabel(String classificationStatus) {
        return "INSUFFICIENT_EVIDENCE".equals(classificationStatus) ? "LIMITED"
                : "UNCERTAIN".equals(classificationStatus) ? "UNCERTAIN" : "REVIEWED";
    }

}
