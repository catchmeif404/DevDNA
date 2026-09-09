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
                <g transform="translate(%d %d) scale(%.2f)" fill="none" stroke="#a32b2b"
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
        return fingerprint(x, y, scale * 0.26);
    }
}
