package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/**
 * Renders the section 16 share card as SVG (no headless browser / raster lib needed for MVP).
 *
 * <p>Uses the same per-type accent color and dark→accent gradient language as {@link
 * BadgeGenerator} (previously this card ignored developer type entirely and looked identical for
 * every result) and adds a stat-box row (commits/repos/PRs) so the exported image carries the same
 * information as the result page itself, not just the headline type.
 */
@Component
public class ShareCardGenerator {

    public String generate(AnalysisResult result) {
        String emoji = DeveloperTypeMeta.emoji(result.getDeveloperType());
        String label = DeveloperTypeMeta.label(result.getDeveloperType());
        String tagline = DeveloperTypeMeta.tagline(result.getDeveloperType());
        String accentColor = DeveloperTypeMeta.color(result.getDeveloperType());
        String topLanguage = result.getTopLanguage() == null ? "N/A" : result.getTopLanguage().toUpperCase();
        int commits = result.getTotalCommits() == null ? 0 : result.getTotalCommits();
        int repositories = result.getTotalRepositories() == null ? 0 : result.getTotalRepositories();
        int pullRequests = result.getTotalPullRequests() == null ? 0 : result.getTotalPullRequests();

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="600" height="800" viewBox="0 0 600 800">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="#0f172a"/>
                      <stop offset="65%%" stop-color="#0f172a"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <radialGradient id="glow" cx="50%%" cy="50%%" r="50%%">
                      <stop offset="0%%" stop-color="%s" stop-opacity="0.55"/>
                      <stop offset="100%%" stop-color="%s" stop-opacity="0"/>
                    </radialGradient>
                  </defs>
                  <rect x="1" y="1" width="598" height="798" rx="28" fill="url(#bg)" stroke="%s" stroke-opacity="0.6" stroke-width="2"/>
                  <text x="300" y="66" text-anchor="middle" font-family="system-ui, sans-serif" font-size="20" letter-spacing="7" fill="%s">DEV DNA</text>
                  <text x="300" y="94" text-anchor="middle" font-family="system-ui, sans-serif" font-size="16" fill="#94a3b8">@%s</text>

                  <circle cx="300" cy="220" r="98" fill="url(#glow)"/>
                  <text x="300" y="248" text-anchor="middle" font-family="system-ui, sans-serif" font-size="92">%s</text>
                  <text x="300" y="342" text-anchor="middle" font-family="system-ui, sans-serif" font-size="36" font-weight="700" fill="#ffffff">%s</text>
                  <text x="300" y="378" text-anchor="middle" font-family="system-ui, sans-serif" font-size="20" fill="#cbd5e1">&#8220;%s&#8221;</text>

                  <rect x="60" y="410" width="480" height="1.5" fill="%s" fill-opacity="0.35"/>

                  <g font-family="system-ui, sans-serif">
                    <rect x="40" y="440" width="160" height="100" rx="16" fill="#1e293b" fill-opacity="0.7" stroke="%s" stroke-opacity="0.35"/>
                    <text x="120" y="490" text-anchor="middle" font-size="32" font-weight="700" fill="#ffffff">%,d</text>
                    <text x="120" y="516" text-anchor="middle" font-size="13" letter-spacing="1" fill="#94a3b8">COMMITS</text>

                    <rect x="220" y="440" width="160" height="100" rx="16" fill="#1e293b" fill-opacity="0.7" stroke="%s" stroke-opacity="0.35"/>
                    <text x="300" y="490" text-anchor="middle" font-size="32" font-weight="700" fill="#ffffff">%,d</text>
                    <text x="300" y="516" text-anchor="middle" font-size="13" letter-spacing="1" fill="#94a3b8">REPOS</text>

                    <rect x="400" y="440" width="160" height="100" rx="16" fill="#1e293b" fill-opacity="0.7" stroke="%s" stroke-opacity="0.35"/>
                    <text x="480" y="490" text-anchor="middle" font-size="32" font-weight="700" fill="#ffffff">%,d</text>
                    <text x="480" y="516" text-anchor="middle" font-size="13" letter-spacing="1" fill="#94a3b8">PULL REQS</text>
                  </g>

                  <rect x="220" y="576" width="160" height="36" rx="18" fill="%s" fill-opacity="0.16" stroke="%s" stroke-opacity="0.5"/>
                  <text x="300" y="600" text-anchor="middle" font-family="system-ui, sans-serif" font-size="15" font-weight="600" letter-spacing="1" fill="%s">%s</text>

                  <text x="300" y="750" text-anchor="middle" font-family="system-ui, sans-serif" font-size="15" letter-spacing="2" fill="#64748b">devdna.catchmeif404.com</text>
                </svg>
                """
                .formatted(
                        accentColor,
                        accentColor, accentColor,
                        accentColor,
                        accentColor,
                        result.getGithubUsername(),
                        emoji,
                        label,
                        tagline,
                        accentColor,
                        accentColor, commits,
                        accentColor, repositories,
                        accentColor, pullRequests,
                        accentColor, accentColor,
                        accentColor, topLanguage);
    }
}
