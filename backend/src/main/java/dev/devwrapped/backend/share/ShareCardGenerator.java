package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Renders the section 16 share card as SVG (no headless browser / raster lib needed for MVP). */
@Component
public class ShareCardGenerator {

    private static final Map<String, String> EMOJI = Map.ofEntries(
            Map.entry("NIGHT_OWL", "🌙"),
            Map.entry("BUG_SLAYER", "🔥"),
            Map.entry("BUILDER", "🏗"),
            Map.entry("POLYGLOT", "🌐"),
            Map.entry("WEEKEND_WARRIOR", "🎉"),
            Map.entry("REFACTOR_MASTER", "🧹"),
            Map.entry("DOCUMENTARIAN", "📚"),
            Map.entry("TESTER", "🧪"),
            Map.entry("EXPLORER", "🧭"),
            Map.entry("COLLABORATOR", "🤝"));

    private static final Map<String, String> LABEL = Map.ofEntries(
            Map.entry("NIGHT_OWL", "NIGHT OWL"),
            Map.entry("BUG_SLAYER", "BUG SLAYER"),
            Map.entry("BUILDER", "BUILDER"),
            Map.entry("POLYGLOT", "POLYGLOT"),
            Map.entry("WEEKEND_WARRIOR", "WEEKEND WARRIOR"),
            Map.entry("REFACTOR_MASTER", "REFACTOR MASTER"),
            Map.entry("DOCUMENTARIAN", "DOCUMENTARIAN"),
            Map.entry("TESTER", "TESTER"),
            Map.entry("EXPLORER", "EXPLORER"),
            Map.entry("COLLABORATOR", "COLLABORATOR"));

    private static final Map<String, String> TAGLINE = Map.ofEntries(
            Map.entry("NIGHT_OWL", "새벽에 강한 개발자"),
            Map.entry("BUG_SLAYER", "버그를 사냥하는 개발자"),
            Map.entry("BUILDER", "꾸준히 만들어가는 개발자"),
            Map.entry("POLYGLOT", "여러 언어를 넘나드는 개발자"),
            Map.entry("WEEKEND_WARRIOR", "주말에 불타오르는 개발자"),
            Map.entry("REFACTOR_MASTER", "코드를 갈고 닦는 개발자"),
            Map.entry("DOCUMENTARIAN", "기록을 남기는 개발자"),
            Map.entry("TESTER", "테스트로 증명하는 개발자"),
            Map.entry("EXPLORER", "여러 저장소를 넘나드는 개발자"),
            Map.entry("COLLABORATOR", "협업으로 성장하는 개발자"));

    public String generate(AnalysisResult result) {
        String emoji = EMOJI.getOrDefault(result.getDeveloperType(), "✨");
        String label = LABEL.getOrDefault(result.getDeveloperType(), result.getDeveloperType());
        String tagline = TAGLINE.getOrDefault(result.getDeveloperType(), "고유한 개발 스타일을 가진 개발자");
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
