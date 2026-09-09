package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/** A 600 x 800 dossier assembled from the recorded public activity. */
@Component
public class ShareCardGenerator {
    public String generate(AnalysisResult result) {
        return generate(result, false);
    }

    String generate(AnalysisResult result, boolean sample) {
        boolean pending = result == null;
        AnalysisResult data = pending ? new AnalysisResult() : result;
        String subject = "@" + EvidenceSvg.clean(data.getGithubUsername(), "UNKNOWN", 39);
        String type = DeveloperTypeMeta.label(data.getDeveloperType());
        String title = "DevDNA / " + (sample ? "SAMPLE / " : "") + "Case file / " + subject + " / " + type;
        return EvidenceSvg.open(600, 800, title) + """
                  <rect x="0.5" y="0.5" width="599" height="799" fill="#e8dcc3" stroke="#241f1a"/>
                  <rect x="20" y="20" width="560" height="760" fill="#f1e9d2" stroke="#241f1a" stroke-opacity="0.3"/>
                  <path d="M40 69 H560 M40 211 H560 M40 313 H560 M40 548 H560 M40 650 H560 M40 727 H560"
                        fill="none" stroke="#241f1a" stroke-opacity="0.35"/>
                """
                + EvidenceSvg.text(40, 49, 13, 300, "CATCHMEIF404 / PUBLIC RECORDS")
                + EvidenceSvg.text(428, 49, 12, 132, sample ? "SAMPLE FILE" : "REF. DNA-404", "#a32b2b")
                + EvidenceSvg.text(40, 117, 38, 520, "DEVDNA")
                + EvidenceSvg.text(40, 143, 13, 520, "DEVELOPER CASE FILE")
                + EvidenceSvg.text(40, 173, 11, 520, sample ? "SUBJECT / SAMPLE - FICTIONAL" : "SUBJECT / GITHUB")
                + EvidenceSvg.text(40, 197, 21, 520, subject)
                + EvidenceSvg.text(40, 239, 11, 520, "01 / TYPE CLASSIFICATION")
                + EvidenceSvg.text(40, 270, 27, 520, type)
                + EvidenceSvg.text(40, 295, 13, 520, DeveloperTypeMeta.tagline(data.getDeveloperType()))
                + EvidenceSvg.text(40, 340, 11, 520, "02 / TYPE EVIDENCE MARK")
                + EvidenceSvg.typeMark(data.getDeveloperType(), 62, 357, 2.4)
                + EvidenceSvg.text(288, 382, 12, 272, "EVIDENCE: PUBLIC ACTIVITY")
                + EvidenceSvg.text(288, 407, 12, 272, "SOURCE: GITHUB")
                + EvidenceSvg.text(288, 432, 11, 272, "TYPE EMBLEM")
                + "<g transform=\"rotate(-7 415 490)\" fill=\"none\" stroke=\"#a32b2b\">"
                + "<rect x=\"306\" y=\"462\" width=\"218\" height=\"56\" stroke-width=\"2\"/>"
                + "<rect x=\"311\" y=\"467\" width=\"208\" height=\"46\"/>"
                + EvidenceSvg.text(327, 499, 26, 176, sample ? "SAMPLE" : pending ? "PENDING" : "REVIEWED", "#a32b2b")
                + "</g>"
                + EvidenceSvg.text(40, 574, 11, 520, sample ? "03 / SAMPLE COUNTS" : "03 / RECORDED COUNTS")
                + "<path d=\"M211 587 V634 M387 587 V634\" stroke=\"#241f1a\" stroke-opacity=\"0.3\"/>"
                + EvidenceSvg.text(40, 608, 24, 158, EvidenceSvg.count(data.getTotalCommits()))
                + EvidenceSvg.text(226, 608, 24, 146, EvidenceSvg.count(data.getTotalRepositories()))
                + EvidenceSvg.text(402, 608, 24, 158, EvidenceSvg.count(data.getTotalPullRequests()))
                + EvidenceSvg.text(40, 630, 11, 158, "COMMITS")
                + EvidenceSvg.text(226, 630, 11, 146, "REPOSITORIES")
                + EvidenceSvg.text(402, 630, 11, 158, "PULL REQUESTS")
                + EvidenceSvg.text(40, 677, 11, 520, "04 / PRIMARY LANGUAGE")
                + EvidenceSvg.text(40, 705, 19, 520, EvidenceSvg.clean(data.getTopLanguage(), "UNRECORDED", 40))
                + EvidenceSvg.text(40, 751, 12, 520,
                        sample ? "SAMPLE / Fictional subject. Illustrative counts."
                                : pending ? "No public analysis on file." : "You found the file. You didn't find me.")
                + EvidenceSvg.text(40, 769, 10, 520, "devdna.catchmeif404.com / catchmeif404")
                + "</svg>\n";
    }
}
