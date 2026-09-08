package dev.devwrapped.backend.share;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.springframework.stereotype.Component;

/**
 * Renders a rounded "achievement chip" SVG badge for embedding in a GitHub README (section 17) —
 * an animated vector animal mascot plus a two-line label, not a flat shields.io metrics rectangle,
 * so it reads as a small collectible rather than a build-status indicator.
 * Text width is estimated (no real text-metrics available server-side) rather than measured.
 */
@Component
public class BadgeGenerator {

    private static final int HEIGHT = 58;
    private static final int ICON_SIZE = 48;
    private static final int MARGIN = 5;
    private static final int TEXT_X = MARGIN + ICON_SIZE + 12;
    private static final String BG_START = "#0f172a";
    private static final String NO_DATA_COLOR = "#6b7280";
    private static final String NO_DATA_TEXT = "no data yet";

    public String generateForResult(AnalysisResult result, Integer founderRank) {
        String line2 = "%s · %s · %s".formatted(
                DeveloperTypeMeta.label(result.getDeveloperType()),
                achievement(result),
                highlightStat(result));
        return render(result.getDeveloperType(), line2, DeveloperTypeMeta.color(result.getDeveloperType()), founderRank);
    }

    public String generateNoData() {
        return render("NO_DATA", NO_DATA_TEXT, NO_DATA_COLOR, null);
    }

    private String achievement(AnalysisResult result) {
        String language = result.getTopLanguage() == null ? "open-source" : result.getTopLanguage();
        return switch (result.getDeveloperType()) {
            case "NIGHT_OWL" -> "Late-night %s streak".formatted(language);
            case "BUG_SLAYER" -> "Bug-hunting %s mode".formatted(language);
            case "BUILDER" -> "Shipping %s energy".formatted(language);
            case "POLYGLOT" -> "Multi-stack explorer";
            case "WEEKEND_WARRIOR" -> "Weekend shipping mode";
            case "REFACTOR_MASTER" -> "Clean-code specialist";
            case "DOCUMENTARIAN" -> "Docs-first builder";
            case "TESTER" -> "Quality guard mode";
            case "EXPLORER" -> "Repo explorer";
            case "COLLABORATOR" -> "Team-powered maker";
            default -> "%s signature".formatted(language);
        };
    }

    private String highlightStat(AnalysisResult result) {
        int commits = result.getTotalCommits() == null ? 0 : result.getTotalCommits();
        int repositories = result.getTotalRepositories() == null ? 0 : result.getTotalRepositories();
        int pullRequests = result.getTotalPullRequests() == null ? 0 : result.getTotalPullRequests();

        double commitScore = commits / 1000.0;
        double repoScore = repositories / 20.0;
        double prScore = pullRequests / 40.0;

        if (prScore >= commitScore && prScore >= repoScore && pullRequests > 0) {
            return compact(pullRequests) + " PRs";
        }
        if (repoScore >= commitScore && repositories > 0) {
            return compact(repositories) + " repos";
        }
        return compact(commits) + " commits";
    }

    private String compact(int value) {
        if (value >= 1_000) {
            return "%.1fk".formatted(value / 1000.0).replace(".0k", "k");
        }
        return "%,d".formatted(value);
    }

    private String render(String developerType, String line2, String accentColor, Integer founderRank) {
        String line1 = "DEVDNA";
        int line1Width = line1.length() * 6 + 12;
        int line2Width = line2.length() * 8 + 10;
        int totalWidth = TEXT_X + Math.max(line1Width, line2Width) + 18;
        int iconCenter = MARGIN + ICON_SIZE / 2;
        String mascot = animalMascot(developerType, MARGIN, accentColor);
        String ariaLabel = founderRank == null
                ? escapeXml(line2)
                : "%s — Founding member #%d".formatted(escapeXml(line2), founderRank);
        String founderRibbon = founderRank == null ? "" : founderCrown(founderRank);

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" role="img" aria-label="DevDNA: %s">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="70%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                    <linearGradient id="shine" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%%" stop-color="#ffffff" stop-opacity="0"/>
                      <stop offset="45%%" stop-color="#ffffff" stop-opacity="0.18"/>
                      <stop offset="100%%" stop-color="#ffffff" stop-opacity="0"/>
                    </linearGradient>
                    <clipPath id="clip">
                      <rect x="0.5" y="0.5" width="%d" height="%d" rx="%d"/>
                    </clipPath>
                  </defs>
                  <rect x="0.5" y="0.5" width="%d" height="%d" rx="%d" fill="url(#bg)" stroke="%s" stroke-opacity="0.7"/>
                  <g clip-path="url(#clip)">
                    <rect x="-56" y="0" width="38" height="%d" fill="url(#shine)" transform="skewX(-18)">
                      <animate attributeName="x" values="-56;%d" dur="4s" repeatCount="indefinite"/>
                    </rect>
                  </g>
                  <circle cx="%d" cy="%d" r="%d" fill="#020617" fill-opacity="0.45" stroke="#ffffff" stroke-opacity="0.16"/>
                  <circle cx="%d" cy="%d" r="13" fill="%s" fill-opacity="0.16">
                    <animate attributeName="r" values="11;15;11" dur="2.4s" repeatCount="indefinite"/>
                    <animate attributeName="fill-opacity" values="0.10;0.24;0.10" dur="2.4s" repeatCount="indefinite"/>
                  </circle>
                  %s
                  <g font-family="Verdana,Geneva,sans-serif" fill="#fff">
                    <text x="%d" y="22" font-size="9" letter-spacing="1.2" fill-opacity="0.58">%s</text>
                    <text x="%d" y="40" font-size="14" font-weight="bold">%s</text>
                  </g>
                  %s
                </svg>
                """
                .formatted(
                        totalWidth, HEIGHT, ariaLabel,
                        BG_START, BG_START, accentColor,
                        totalWidth - 1, HEIGHT - 1, HEIGHT / 2,
                        totalWidth - 1, HEIGHT - 1, HEIGHT / 2, accentColor,
                        HEIGHT, totalWidth + 56,
                        iconCenter, HEIGHT / 2, ICON_SIZE / 2,
                        iconCenter, HEIGHT / 2, accentColor,
                        mascot,
                        TEXT_X, line1,
                        TEXT_X, escapeXml(line2),
                        founderRibbon);
    }

    /**
     * Small gold crown resting on top of the mascot's icon circle, marking one of the first 3
     * users. Anchored to the icon circle's fixed center (MARGIN + ICON_SIZE / 2, always 29,29
     * regardless of developer type) rather than to any mascot's own artwork, so it works
     * identically for all 10 mascots with no per-type tuning.
     */
    private String founderCrown(int founderRank) {
        int cx = MARGIN + ICON_SIZE / 2;
        int topY = 8;
        return """
                <g transform="translate(%d %d)">
                  <title>Founding member #%d</title>
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.5;0 0" dur="2s" repeatCount="indefinite"/>
                    <path d="M-11 7 L-11 -1 L-5.5 4 L0 -6 L5.5 4 L11 -1 L11 7 Z" fill="#facc15" stroke="#78350f" stroke-width="1.1" stroke-linejoin="round"/>
                    <rect x="-11" y="4.6" width="22" height="3.4" rx="1" fill="#eab308" stroke="#78350f" stroke-width="0.8"/>
                    <circle cx="-11" cy="-1" r="1.7" fill="#fde68a"/>
                    <circle cx="0" cy="-6" r="2" fill="#fde68a"/>
                    <circle cx="11" cy="-1" r="1.7" fill="#fde68a"/>
                  </g>
                </g>
                """.formatted(cx, topY, founderRank);
    }

    private String animalMascot(String developerType, int x, String accentColor) {
        return switch (developerType) {
            case "NIGHT_OWL" -> owlMascot(x, accentColor);
            case "BUG_SLAYER" -> catMascot(x, accentColor);
            case "BUILDER" -> beaverMascot(x, accentColor);
            case "POLYGLOT" -> parrotMascot(x, accentColor);
            case "WEEKEND_WARRIOR" -> otterMascot(x, accentColor);
            case "REFACTOR_MASTER" -> foxMascot(x, accentColor);
            case "DOCUMENTARIAN" -> elephantMascot(x, accentColor);
            case "TESTER" -> mouseMascot(x, accentColor);
            case "EXPLORER" -> turtleMascot(x, accentColor);
            case "COLLABORATOR" -> penguinMascot(x + 4, accentColor);
            default -> idleCritterFeatures(accentColor);
        };
    }

    private String owlMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 7)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.4;0 0" dur="2.5s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="48" rx="15" ry="3" fill="#000" opacity="0.22"/>
                    <path d="M8 24 C8 14 14 8 22 8 C30 8 36 14 36 24 C36 39 29 45 22 45 C15 45 8 39 8 24 Z" fill="#7c3aed"/>
                    <path d="M9 18 L14 4 L22 13 L30 4 L35 18" fill="#a78bfa"/>
                    <ellipse cx="16" cy="24" rx="6" ry="7" fill="#f8fafc"/>
                    <ellipse cx="28" cy="24" rx="6" ry="7" fill="#f8fafc"/>
                    <ellipse cx="16" cy="24" rx="2.4" ry="2.4" fill="#020617">
                      <animate attributeName="ry" values="2.4;0.45;2.4" dur="3.2s" repeatCount="indefinite"/>
                    </ellipse>
                    <ellipse cx="28" cy="24" rx="2.4" ry="2.4" fill="#020617">
                      <animate attributeName="ry" values="2.4;0.45;2.4" dur="3.2s" repeatCount="indefinite"/>
                    </ellipse>
                    <path d="M20 29 L24 29 L22 33 Z" fill="#facc15"/>
                    <path d="M13 38 Q22 42 31 38" fill="none" stroke="#ddd6fe" stroke-width="2"/>
                    <path d="M39 8 Q34 9 32 14" fill="none" stroke="%s" stroke-width="2">
                      <animateTransform attributeName="transform" type="rotate" values="0 35 11;12 35 11;0 35 11" dur="2.2s" repeatCount="indefinite"/>
                    </path>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String catMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 7)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1;0 0" dur="2.1s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="48" rx="15" ry="3" fill="#000" opacity="0.22"/>
                    <ellipse cx="22" cy="34" rx="13" ry="10" fill="#fb923c"/>
                    <path d="M10 21 L13 7 L21 16 L29 7 L34 21 Z" fill="#f97316"/>
                    <circle cx="22" cy="24" r="13" fill="#f97316"/>
                    <ellipse cx="17" cy="23" rx="2.2" ry="2.2" fill="#020617">
                      <animate attributeName="ry" values="2.2;0.35;2.2" dur="2.7s" repeatCount="indefinite"/>
                    </ellipse>
                    <ellipse cx="27" cy="23" rx="2.2" ry="2.2" fill="#020617">
                      <animate attributeName="ry" values="2.2;0.35;2.2" dur="2.7s" repeatCount="indefinite"/>
                    </ellipse>
                    <path d="M22 26 L20 29 H24 Z" fill="#7f1d1d"/>
                    <path d="M18 32 Q22 35 26 32" fill="none" stroke="#7f1d1d" stroke-width="1.6"/>
                    <path d="M12 27 H4 M13 31 H5 M32 27 H40 M31 31 H39" stroke="#7f1d1d" stroke-width="1"/>
                    <path d="M34 37 C45 33 39 21 32 29" fill="none" stroke="%s" stroke-width="3">
                      <animateTransform attributeName="transform" type="rotate" values="0 34 37;-16 34 37;0 34 37" dur="1.35s" repeatCount="indefinite"/>
                    </path>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String beaverMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 7)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -0.8;0 0" dur="2.4s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="48" rx="15" ry="3" fill="#000" opacity="0.22"/>
                    <ellipse cx="22" cy="34" rx="14" ry="10" fill="#92400e"/>
                    <ellipse cx="22" cy="39" rx="8" ry="6" fill="#fbbf24" opacity="0.35"/>
                    <circle cx="22" cy="22" r="12" fill="#a16207"/>
                    <circle cx="14" cy="15" r="4" fill="#92400e"/>
                    <circle cx="30" cy="15" r="4" fill="#92400e"/>
                    <ellipse cx="18" cy="22" rx="2" ry="2" fill="#020617"/>
                    <ellipse cx="26" cy="22" rx="2" ry="2" fill="#020617"/>
                    <path d="M20 25 L24 25 L22 28 Z" fill="#451a03"/>
                    <rect x="18" y="30" width="3.5" height="5" rx="0.8" fill="#f8fafc"/>
                    <rect x="22.5" y="30" width="3.5" height="5" rx="0.8" fill="#f8fafc"/>
                    <path d="M32 34 L40 28" stroke="%s" stroke-width="3">
                      <animateTransform attributeName="transform" type="rotate" values="0 32 34;18 32 34;0 32 34" dur="1s" repeatCount="indefinite"/>
                    </path>
                    <path d="M36 25 L43 32" stroke="#facc15" stroke-width="2"/>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String parrotMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 6)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.5;0 0" dur="1.8s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="49" rx="15" ry="3" fill="#000" opacity="0.2"/>
                    <ellipse cx="20" cy="35" rx="11" ry="12" fill="#22d3ee"/>
                    <path d="M11 31 C1 27 3 44 14 41" fill="%s">
                      <animateTransform attributeName="transform" type="rotate" values="0 12 34;-16 12 34;0 12 34" dur="0.9s" repeatCount="indefinite"/>
                    </path>
                    <circle cx="22" cy="21" r="12" fill="#06b6d4"/>
                    <path d="M23 8 C26 2 33 4 33 11 C30 8 26 8 23 8 Z" fill="#ef4444"/>
                    <path d="M31 21 C42 18 43 28 31 29 Z" fill="#facc15"/>
                    <ellipse cx="19" cy="20" rx="2.2" ry="2.2" fill="#020617"/>
                    <circle cx="19.7" cy="19.2" r="0.65" fill="#ffffff"/>
                    <path d="M16 31 Q20 34 25 31" fill="none" stroke="#155e75" stroke-width="1.6"/>
                    <path d="M20 45 L17 50 M25 44 L27 50" stroke="#f59e0b" stroke-width="2.2"/>
                  </g>
                </g>
                """.formatted(x + 5, accentColor);
    }

    private String otterMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 8)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1;0 0" dur="2.1s" repeatCount="indefinite"/>
                    <path d="M2 44 Q8 40 14 44 T26 44 T40 44" fill="none" stroke="%s" stroke-width="2" opacity="0.9">
                      <animate attributeName="d" values="M2 44 Q8 40 14 44 T26 44 T40 44;M2 42 Q8 46 14 42 T26 42 T40 42;M2 44 Q8 40 14 44 T26 44 T40 44" dur="1.6s" repeatCount="indefinite"/>
                    </path>
                    <ellipse cx="22" cy="35" rx="15" ry="9" fill="#92400e"/>
                    <ellipse cx="22" cy="36" rx="8" ry="5" fill="#fed7aa" opacity="0.55"/>
                    <circle cx="22" cy="22" r="12" fill="#a16207"/>
                    <circle cx="14" cy="16" r="4" fill="#78350f"/>
                    <circle cx="30" cy="16" r="4" fill="#78350f"/>
                    <ellipse cx="18" cy="22" rx="2" ry="2" fill="#020617"/>
                    <ellipse cx="26" cy="22" rx="2" ry="2" fill="#020617"/>
                    <path d="M21 25 L23 25 L22 27 Z" fill="#020617"/>
                    <path d="M17 30 Q22 33 27 30" fill="none" stroke="#451a03" stroke-width="1.5"/>
                    <path d="M9 35 Q5 31 7 26" fill="none" stroke="#78350f" stroke-width="3">
                      <animateTransform attributeName="transform" type="rotate" values="0 9 35;-12 9 35;0 9 35" dur="1.3s" repeatCount="indefinite"/>
                    </path>
                  </g>
                </g>
                """.formatted(x + 5, accentColor);
    }

    private String foxMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 7)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.1;0 0" dur="2.2s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="48" rx="15" ry="3" fill="#000" opacity="0.22"/>
                    <path d="M9 22 L13 5 L22 16 L31 5 L35 22 C35 37 29 45 22 45 C15 45 9 37 9 22 Z" fill="#fb923c"/>
                    <path d="M13 23 L22 35 L31 23 C29 40 15 40 13 23 Z" fill="#fff7ed"/>
                    <ellipse cx="18" cy="23" rx="2" ry="2" fill="#020617"/>
                    <ellipse cx="26" cy="23" rx="2" ry="2" fill="#020617"/>
                    <path d="M20 28 L24 28 L22 31 Z" fill="#020617"/>
                    <path d="M18 34 Q22 36 26 34" fill="none" stroke="#7c2d12" stroke-width="1.5"/>
                    <path d="M34 38 C46 32 40 17 31 26" fill="none" stroke="%s" stroke-width="5">
                      <animateTransform attributeName="transform" type="rotate" values="0 34 38;13 34 38;0 34 38" dur="1.7s" repeatCount="indefinite"/>
                    </path>
                    <path d="M39 23 L44 20" stroke="#ffffff" stroke-width="3"/>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String elephantMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 7)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -0.8;0 0" dur="2.6s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="48" rx="16" ry="3" fill="#000" opacity="0.2"/>
                    <ellipse cx="22" cy="34" rx="15" ry="11" fill="#94a3b8"/>
                    <circle cx="22" cy="22" r="13" fill="#cbd5e1"/>
                    <ellipse cx="8" cy="23" rx="7" ry="10" fill="#94a3b8"/>
                    <ellipse cx="36" cy="23" rx="7" ry="10" fill="#94a3b8"/>
                    <ellipse cx="18" cy="21" rx="2" ry="2" fill="#020617"/>
                    <ellipse cx="26" cy="21" rx="2" ry="2" fill="#020617"/>
                    <path d="M22 26 C22 36 33 35 29 27" fill="none" stroke="%s" stroke-width="4">
                      <animate attributeName="d" values="M22 26 C22 36 33 35 29 27;M22 26 C22 37 13 36 16 28;M22 26 C22 36 33 35 29 27" dur="2.1s" repeatCount="indefinite"/>
                    </path>
                    <path d="M16 37 L14 44 M28 37 L30 44" stroke="#64748b" stroke-width="3"/>
                    <path d="M13 12 Q21 7 31 12" fill="none" stroke="#f8fafc" stroke-width="1.5" opacity="0.45"/>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String mouseMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 8)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.2;0 0" dur="2s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="47" rx="14" ry="3" fill="#000" opacity="0.2"/>
                    <ellipse cx="22" cy="34" rx="12" ry="9" fill="#c4b5fd"/>
                    <circle cx="22" cy="22" r="12" fill="#ddd6fe"/>
                    <circle cx="12" cy="13" r="6" fill="#c4b5fd"/>
                    <circle cx="32" cy="13" r="6" fill="#c4b5fd"/>
                    <circle cx="12" cy="13" r="3.2" fill="#f5d0fe"/>
                    <circle cx="32" cy="13" r="3.2" fill="#f5d0fe"/>
                    <ellipse cx="18" cy="22" rx="2" ry="2" fill="#020617"/>
                    <ellipse cx="26" cy="22" rx="2" ry="2" fill="#020617"/>
                    <path d="M21 25 L23 25 L22 27 Z" fill="#ec4899"/>
                    <path d="M18 31 Q22 33 26 31" fill="none" stroke="#6d28d9" stroke-width="1.4"/>
                    <path d="M31 38 C44 36 42 23 34 28" fill="none" stroke="%s" stroke-width="2">
                      <animateTransform attributeName="transform" type="rotate" values="0 31 38;-12 31 38;0 31 38" dur="1.4s" repeatCount="indefinite"/>
                    </path>
                    <circle cx="38" cy="10" r="1.5" fill="#ffffff">
                      <animate attributeName="cy" values="10;5;10" dur="1.1s" repeatCount="indefinite"/>
                    </circle>
                  </g>
                </g>
                """.formatted(x + 4, accentColor);
    }

    private String turtleMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 9)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;1 0;0 0" dur="2.4s" repeatCount="indefinite"/>
                    <ellipse cx="22" cy="46" rx="16" ry="3" fill="#000" opacity="0.2"/>
                    <ellipse cx="20" cy="31" rx="15" ry="11" fill="#65a30d"/>
                    <path d="M8 31 Q20 17 32 31 Q20 43 8 31 Z" fill="#84cc16"/>
                    <path d="M10 31 H30 M20 20 V41 M13 25 L27 37 M27 25 L13 37" stroke="#365314" stroke-width="1.3" opacity="0.8"/>
                    <circle cx="35" cy="28" r="6" fill="%s">
                      <animate attributeName="cx" values="34;37;34" dur="1.8s" repeatCount="indefinite"/>
                    </circle>
                    <ellipse cx="37" cy="27" rx="1.4" ry="1.4" fill="#020617"/>
                    <path d="M33 32 Q36 34 39 32" fill="none" stroke="#14532d" stroke-width="1"/>
                    <path d="M8 40 L5 44 M16 40 L13 45 M25 40 L28 45 M32 38 L36 42" stroke="#4d7c0f" stroke-width="2.4"/>
                  </g>
                </g>
                """.formatted(x + 3, accentColor);
    }

    private String penguinMascot(int x, String accentColor) {
        return """
                <g transform="translate(%d 6)" stroke-linecap="round" stroke-linejoin="round">
                  <g>
                    <animateTransform attributeName="transform" type="translate" values="0 0;0 -1.2;0 0" dur="2.3s" repeatCount="indefinite"/>
                    <ellipse cx="20" cy="48" rx="15" ry="3" fill="#000000" opacity="0.22"/>
                    <path d="M9 26 C4 29 3 38 8 43 C12 47 28 47 32 43 C37 38 36 29 31 26 C30 16 26 9 20 9 C14 9 10 16 9 26 Z" fill="#111827"/>
                    <ellipse cx="20" cy="31" rx="10.5" ry="13.5" fill="#f8fafc"/>
                    <path d="M9 29 C2 30 1 38 7 40" fill="none" stroke="#111827" stroke-width="5">
                      <animateTransform attributeName="transform" type="rotate" values="0 9 29;-10 9 29;0 9 29" dur="1.8s" repeatCount="indefinite"/>
                    </path>
                    <path d="M31 28 C38 26 40 17 35 15" fill="none" stroke="#111827" stroke-width="5">
                      <animateTransform attributeName="transform" type="rotate" values="0 31 28;18 31 28;0 31 28" dur="1.25s" repeatCount="indefinite"/>
                    </path>
                    <ellipse cx="16" cy="20" rx="2.2" ry="2.2" fill="#020617">
                      <animate attributeName="ry" values="2.2;0.45;2.2" dur="3.1s" repeatCount="indefinite"/>
                    </ellipse>
                    <ellipse cx="24" cy="20" rx="2.2" ry="2.2" fill="#020617">
                      <animate attributeName="ry" values="2.2;0.45;2.2" dur="3.1s" repeatCount="indefinite"/>
                    </ellipse>
                    <circle cx="16.7" cy="19.2" r="0.65" fill="#ffffff"/>
                    <circle cx="24.7" cy="19.2" r="0.65" fill="#ffffff"/>
                    <path d="M19 23 L23 23 L21 26 Z" fill="#f59e0b"/>
                    <circle cx="12.7" cy="25" r="1.7" fill="#fb7185" opacity="0.72"/>
                    <circle cx="27.3" cy="25" r="1.7" fill="#fb7185" opacity="0.72"/>
                    <path d="M16 31 Q20 34 24 31" fill="none" stroke="#d1d5db" stroke-width="1.4"/>
                    <path d="M12 45 C15 41 18 41 20 45" fill="#f59e0b"/>
                    <path d="M20 45 C22 41 26 41 29 45" fill="#f59e0b"/>
                    <path d="M35 8 C38 5 43 7 43 12 C43 17 35 19 35 24 C35 19 27 17 27 12 C27 7 32 5 35 8 Z" fill="%s" opacity="0.95">
                      <animateTransform attributeName="transform" type="scale" values="0.9;1.08;0.9" dur="1.5s" repeatCount="indefinite"/>
                    </path>
                  </g>
                </g>
                """.formatted(x, accentColor);
    }

    private String idleCritterFeatures(String accentColor) {
        return """
                <circle cx="10" cy="8" r="4" fill="#cbd5e1"/>
                <circle cx="24" cy="8" r="4" fill="#cbd5e1"/>
                <path d="M24 21 Q30 18 29 12" fill="none" stroke="%s" stroke-width="2">
                  <animateTransform attributeName="transform" type="rotate" values="0 24 21;10 24 21;0 24 21" dur="2s" repeatCount="indefinite"/>
                </path>
                """.formatted(accentColor);
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
