package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/** Renders the section 16 share card as SVG (no headless browser / raster lib needed for MVP). */
@Component
public class ShareCardGenerator {

    public String generate(AnalysisResult result) {
        String emoji = DeveloperTypeMeta.emoji(result.getDeveloperType());
        String label = DeveloperTypeMeta.label(result.getDeveloperType());
        String tagline = DeveloperTypeMeta.tagline(result.getDeveloperType());
        String topLanguage = result.getTopLanguage() == null ? "N/A" : result.getTopLanguage();

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="600" height="800" viewBox="0 0 600 800">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%%" stop-color="#0f172a"/>
                      <stop offset="100%%" stop-color="#1e1b4b"/>
                    </linearGradient>
                  </defs>
                  <rect width="600" height="800" fill="url(#bg)" rx="24"/>
                  <text x="300" y="90" text-anchor="middle" font-family="system-ui, sans-serif" font-size="22" letter-spacing="6" fill="#a5b4fc">DEV WRAPPED</text>
                  <text x="300" y="230" text-anchor="middle" font-family="system-ui, sans-serif" font-size="64">%s</text>
                  <text x="300" y="300" text-anchor="middle" font-family="system-ui, sans-serif" font-size="34" font-weight="700" fill="#ffffff">%s</text>
                  <text x="300" y="420" text-anchor="middle" font-family="system-ui, sans-serif" font-size="40" font-weight="700" fill="#facc15">%,d COMMITS</text>
                  <text x="300" y="470" text-anchor="middle" font-family="system-ui, sans-serif" font-size="24" fill="#e2e8f0">%s MAIN</text>
                  <text x="300" y="600" text-anchor="middle" font-family="system-ui, sans-serif" font-size="22" fill="#cbd5e1">"%s"</text>
                  <text x="300" y="750" text-anchor="middle" font-family="system-ui, sans-serif" font-size="16" letter-spacing="2" fill="#64748b">devwrapped.dev</text>
                </svg>
                """
                .formatted(emoji, label, result.getTotalCommits(), topLanguage.toUpperCase(), tagline);
    }
}
