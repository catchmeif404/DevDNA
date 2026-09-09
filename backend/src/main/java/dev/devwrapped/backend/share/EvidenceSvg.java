package dev.devwrapped.backend.share;

import java.util.Locale;

/** Shared, bounded XML text and code-native evidence artwork for the two generators. */
final class EvidenceSvg {
    private EvidenceSvg() {}

    static String clean(String value, String fallback, int limit) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        StringBuilder clean = new StringBuilder();
        // XML 1.0 excludes control characters and lone UTF-16 surrogates.
        value.codePoints().filter(cp -> cp == 9 || cp == 10 || cp == 13
                        || cp >= 0x20 && cp <= 0xD7FF || cp >= 0xE000 && cp <= 0xFFFD
                        || cp >= 0x10000 && cp <= 0x10FFFF)
                .limit(limit + 1L).forEach(cp -> clean.appendCodePoint(Character.isWhitespace(cp) ? ' ' : cp));
        String text = clean.toString().strip();
        if (text.isEmpty()) {
            return fallback;
        }
        return text.codePointCount(0, text.length()) > limit
                ? text.substring(0, text.offsetByCodePoints(0, limit - 3)) + "..." : text;
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    static String count(Integer value) {
        return String.format(Locale.ROOT, "%,d", value == null ? 0 : Math.max(0, value));
    }

    static String open(int width, int height, String title) {
        String safeTitle = escape(clean(title, "DevDNA", 200));
        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d"
                     role="img" aria-label="%s" font-family="'Courier Prime', 'Courier New', monospace"
                     fill="#241f1a" letter-spacing="0">
                  <title>%s</title>
                """.formatted(width, height, width, height, safeTitle, safeTitle);
    }

    static String text(int x, int y, int size, int width, String value) {
        return text(x, y, size, width, value, "#241f1a");
    }

    static String text(int x, int y, int size, int width, String value, String color) {
        String safe = clean(value, "UNRECORDED", 100);
        if (textUnits(safe) * 8 > width) {
            while (textUnits(safe + "...") * 8 > width) {
                safe = safe.substring(0, safe.offsetByCodePoints(safe.length(), -1));
            }
            safe += "...";
        }
        double units = textUnits(safe);
        double fontSize = Math.min(size, width / units);
        // Explicit SVG text metrics keep fallback fonts and wide Unicode glyphs inside each field.
        return String.format(Locale.ROOT,
                "<text x=\"%d\" y=\"%d\" font-size=\"%.2f\" textLength=\"%.2f\" lengthAdjust=\"spacingAndGlyphs\" fill=\"%s\" stroke=\"none\">%s</text>\n",
                x, y, fontSize, Math.min(width, units * fontSize), color, escape(safe));
    }

    private static double textUnits(String value) {
        return value.codePoints().mapToDouble(cp -> cp < 128 ? 0.61 : 1.0).sum();
    }

    static String fingerprint(int x, int y, double scale) {
        return String.format(Locale.ROOT, """
                <g transform="translate(%d %d) scale(%.2f)" fill="none" stroke="#241f1a"
                   stroke-width="2.2" stroke-linecap="round" aria-hidden="true">
                  <path d="M12 92 C12 8 176 4 178 94 C180 143 159 179 144 201"/>
                  <path d="M22 103 C12 28 156 4 167 85 C176 140 149 183 135 201"/>
                  <path d="M25 126 C48 110 13 68 62 40 C104 15 151 43 155 87 C160 140 138 175 123 196"/>
                  <path d="M19 150 C65 122 28 84 68 54 C98 32 137 53 143 88 C151 143 119 181 113 192"/>
                  <path d="M28 163 C81 125 41 86 78 65 C104 50 127 66 132 94 C140 137 114 169 102 189"/>
                  <path d="M40 177 C92 133 58 102 84 79 C103 62 122 87 120 111 C118 145 99 167 90 184"/>
                  <path d="M54 187 C106 137 73 112 92 93 C108 78 117 119 96 149"/>
                  <path d="M71 194 C80 180 91 168 96 158 M94 108 C99 127 87 146 75 160"/>
                  <path d="M3 24 V3 H24 M164 3 H187 V24 M3 180 V208 H24 M164 208 H187 V180"
                        stroke-opacity="0.45" stroke-width="1"/>
                </g>
                """, x, y, scale);
    }

    static String typeMark(String type, int x, int y, double scale) {
        String shape = switch (type == null ? "" : type) {
            case "NIGHT_OWL" -> "<path d=\"M18 34 A16 16 0 1 1 34 18 A12 12 0 1 0 18 34Z\"/>";
            case "BUG_SLAYER" -> "<path d=\"M26 8 V42 M10 16 H42 M10 34 H42 M16 10 L8 4 M36 10 L44 4 M16 40 L8 48 M36 40 L44 48\"/><circle cx=\"26\" cy=\"25\" r=\"13\"/>";
            case "BUILDER" -> "<path d=\"M8 42 H44 M12 42 V26 H24 V42 M28 42 V16 H40 V42 M8 16 H18 V8 H32\"/>";
            case "POLYGLOT" -> "<path d=\"M26 5 L45 16 V38 L26 49 L7 38 V16Z M7 16 L26 27 L45 16 M26 27 V49\"/>";
            case "WEEKEND_WARRIOR" -> "<path d=\"M8 38 L18 18 L28 30 L38 10 L46 38Z\"/>";
            case "REFACTOR_MASTER" -> "<path d=\"M10 10 H27 A9 9 0 0 1 36 19 V39 M36 39 L28 31 M36 39 L44 31 M42 42 H25 A9 9 0 0 1 16 33 V13 M16 13 L8 21 M16 13 L24 21\"/>";
            case "DOCUMENTARIAN" -> "<path d=\"M10 7 H42 V45 H10Z M17 16 H35 M17 25 H35 M17 34 H29\"/>";
            case "TESTER" -> "<path d=\"M8 28 L20 40 L44 12\"/>";
            case "EXPLORER" -> "<circle cx=\"26\" cy=\"26\" r=\"19\"/><path d=\"M26 7 V45 M7 26 H45 M14 14 L38 38 M38 14 L14 38\"/>";
            case "COLLABORATOR" -> "<circle cx=\"17\" cy=\"17\" r=\"7\"/><circle cx=\"35\" cy=\"17\" r=\"7\"/><path d=\"M7 43 C8 30 26 30 27 43 M25 43 C26 30 44 30 45 43 M24 17 H28\"/>";
            default -> "<circle cx=\"26\" cy=\"26\" r=\"19\"/><path d=\"M14 26 H38 M26 14 V38\"/>";
        };
        String frame = "<path d=\"M3 3 H49 V49 H3Z\"/><path d=\"M3 26 H49\"/><path d=\"M26 3 V49\"/><path d=\"M8 8 L44 44\"/><path d=\"M44 8 L8 44\"/>";
        return String.format(Locale.ROOT,
                "<g transform=\"translate(%d %d) scale(%.2f)\" fill=\"none\" stroke=\"#241f1a\" stroke-width=\"2.4\" stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\">%s</g>\n",
                x, y, scale, frame + shape);
    }
}
