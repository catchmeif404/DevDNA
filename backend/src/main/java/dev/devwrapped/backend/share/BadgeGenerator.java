package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/**
 * Renders a rounded "achievement chip" SVG badge for embedding in a GitHub README (section 17) —
 * a big emoji icon in a circle plus a two-line label, not a flat shields.io metrics rectangle,
 * so it reads as a small collectible rather than a build-status indicator.
 * Text width is estimated (no real text-metrics available server-side) rather than measured.
 */
@Component
public class BadgeGenerator {

    private static final int HEIGHT = 40;
    private static final int CIRCLE_DIAMETER = 32;
    private static final int MARGIN = 4;
    private static final int TEXT_X = MARGIN + CIRCLE_DIAMETER + 10;
    private static final String BG_START = "#0f172a";
    private static final String NO_DATA_COLOR = "#6b7280";
    private static final String NO_DATA_EMOJI = "🤷";
    private static final String NO_DATA_TEXT = "no data yet";

    public String generateForResult(AnalysisResult result) {
        String emoji = DeveloperTypeMeta.emoji(result.getDeveloperType());
        String line2 = "%s · %,d commits · %s".formatted(
                DeveloperTypeMeta.label(result.getDeveloperType()),
                result.getTotalCommits(),
                result.getTopLanguage() == null ? "N/A" : result.getTopLanguage());
        return render(emoji, line2, DeveloperTypeMeta.color(result.getDeveloperType()));
    }

    public String generateNoData() {
        return render(NO_DATA_EMOJI, NO_DATA_TEXT, NO_DATA_COLOR);
    }

    private String render(String emoji, String line2, String accentColor) {
        String line1 = "DEVWRAPPED";
        int line1Width = line1.length() * 5 + 10;
        int line2Width = line2.length() * 8 + 10;
        int totalWidth = TEXT_X + Math.max(line1Width, line2Width) + 14;
        int circleCenter = MARGIN + CIRCLE_DIAMETER / 2;

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" role="img" aria-label="DevWrapped: %s">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0" stop-color="%s"/>
                      <stop offset="1" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect x="0.5" y="0.5" width="%d" height="%d" rx="%d" fill="url(#bg)" stroke="%s" stroke-opacity="0.6"/>
                  <circle cx="%d" cy="%d" r="%d" fill="#fff" fill-opacity="0.12"/>
                  <text x="%d" y="%d" text-anchor="middle" font-size="18" dominant-baseline="central">%s</text>
                  <g font-family="Verdana,Geneva,sans-serif" fill="#fff">
                    <text x="%d" y="16" font-size="8" letter-spacing="1" fill-opacity="0.55">%s</text>
                    <text x="%d" y="30" font-size="13" font-weight="bold">%s</text>
                  </g>
                </svg>
                """
                .formatted(
                        totalWidth, HEIGHT, escapeXml(line2),
                        BG_START, accentColor,
                        totalWidth - 1, HEIGHT - 1, HEIGHT / 2, accentColor,
                        circleCenter, HEIGHT / 2, CIRCLE_DIAMETER / 2,
                        circleCenter, HEIGHT / 2, emoji,
                        TEXT_X, line1,
                        TEXT_X, escapeXml(line2));
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
