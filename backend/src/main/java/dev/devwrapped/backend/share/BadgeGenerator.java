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
                founderRank, sample, false);
    }

    public String generateNoData() {
        return render("UNIDENTIFIED", "AWAITING EVIDENCE", "No public analysis on file.",
                "STATUS: no data yet", null, false, true);
    }

    private String render(String subject, String type, String stats, String language,
            Integer founderRank, boolean sample, boolean pending) {
        String founder = founderRank != null && founderRank > 0 ? "Founding member #" + founderRank : "";
        String status = sample ? "SAMPLE" : pending ? "PENDING" : "REVIEWED";
        String title = "DevDNA: " + (sample ? "SAMPLE / " : "") + subject + " / " + type
                + (founder.isEmpty() ? "" : " / " + founder);
        return EvidenceSvg.open(600, 200, title) + """
                  <rect x="0.5" y="0.5" width="599" height="199" fill="#f1e9d2" stroke="#241f1a"/>
                  <rect x="1" y="1" width="598" height="40" fill="#e8dcc3"/>
                  <path d="M16 42 H584 M116 54 V152 M16 162 H584" fill="none" stroke="#241f1a" stroke-opacity="0.35"/>
                """
                + EvidenceSvg.text(18, 27, 16, 340, "DEVDNA / EVIDENCE LABEL")
                + EvidenceSvg.text(455, 27, 13, 125, status, "#a32b2b")
                + EvidenceSvg.fingerprint(22, 56, 0.42)
                + EvidenceSvg.text(130, 65, 10, 448, sample ? "SAMPLE SUBJECT" : "SUBJECT")
                + EvidenceSvg.text(130, 86, 16, 448, subject)
                + EvidenceSvg.text(130, 105, 10, 448, "TYPE")
                + EvidenceSvg.text(130, 127, 19, 448, type)
                + EvidenceSvg.text(130, 149, 12, 448, stats)
                + EvidenceSvg.text(18, 182, 11, founder.isEmpty() ? 560 : 292, language)
                + (founder.isEmpty() ? "" : EvidenceSvg.text(326, 182, 11, 256, founder, "#a32b2b"))
                + "</svg>\n";
    }
}
