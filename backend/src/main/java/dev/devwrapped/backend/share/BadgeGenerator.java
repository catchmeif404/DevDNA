package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/**
 * Renders a shields.io-style badge SVG for embedding in a GitHub README (section 17).
 * Text width is estimated (no real text-metrics available server-side) rather than measured.
 */
@Component
public class BadgeGenerator {

    private static final int HEIGHT = 20;
    private static final int LABEL_WIDTH = 96;
    private static final String LABEL_TEXT = "DevWrapped";
    private static final String NO_DATA_TEXT = "no data yet";
    private static final String NO_DATA_COLOR = "#6b7280";
    private static final String VALUE_COLOR = "#6366f1";

    public String generateForResult(AnalysisResult result) {
        String value = "%s %s · %,d commits · %s".formatted(
                DeveloperTypeMeta.emoji(result.getDeveloperType()),
                DeveloperTypeMeta.label(result.getDeveloperType()),
                result.getTotalCommits(),
                result.getTopLanguage() == null ? "N/A" : result.getTopLanguage());
        return render(value, VALUE_COLOR);
    }

    public String generateNoData() {
        return render(NO_DATA_TEXT, NO_DATA_COLOR);
    }

    private String render(String valueText, String valueColor) {
        int valueWidth = Math.max(90, valueText.length() * 7 + 20);
        int totalWidth = LABEL_WIDTH + valueWidth;

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" role="img" aria-label="%s: %s">
                  <linearGradient id="s" x2="0" y2="100%%">
                    <stop offset="0" stop-color="#fff" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                  </linearGradient>
                  <clipPath id="r">
                    <rect width="%d" height="%d" rx="3" fill="#fff"/>
                  </clipPath>
                  <g clip-path="url(#r)">
                    <rect width="%d" height="%d" fill="#1e293b"/>
                    <rect x="%d" width="%d" height="%d" fill="%s"/>
                    <rect width="%d" height="%d" fill="url(#s)"/>
                  </g>
                  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,sans-serif" font-size="11">
                    <text x="%d" y="14">%s</text>
                    <text x="%d" y="14">%s</text>
                  </g>
                </svg>
                """
                .formatted(
                        totalWidth, HEIGHT, LABEL_TEXT, valueText,
                        totalWidth, HEIGHT,
                        totalWidth, HEIGHT,
                        LABEL_WIDTH, valueWidth, HEIGHT, valueColor,
                        totalWidth, HEIGHT,
                        LABEL_WIDTH / 2, LABEL_TEXT,
                        LABEL_WIDTH + valueWidth / 2, escapeXml(valueText));
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
