package dev.devwrapped.backend.share;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SharePreviewTest {
    /** Set -Ddevdna.previewDir through JAVA_TOOL_OPTIONS to regenerate the checked-in SVGs. */
    @Test
    void generatesExplicitlyFictionalPreviews() throws Exception {
        var result = ShareSvgTestSupport.result();
        String badge = new BadgeGenerator().generateForResult(result, 2, true);
        String card = new ShareCardGenerator().generate(result, true);
        for (String svg : new String[] {badge, card}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(document.getDocumentElement().getAttribute("aria-label")).contains("SAMPLE", "@example-dev");
            assertThat(document.getDocumentElement().getTextContent()).contains("SAMPLE", "@example-dev");
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
        assertThat(card).contains("Fictional subject. Illustrative counts.", "SAMPLE COUNTS");
        assertThat(badge).contains("SAMPLE SUBJECT", "Founding member #2");
        String previewDir = System.getProperty("devdna.previewDir");
        if (previewDir != null) {
            Path directory = Path.of(previewDir);
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("sample-badge.svg"), badge);
            Files.writeString(directory.resolve("sample-card.svg"), card);
            String[] types = {"NIGHT_OWL", "BUG_SLAYER", "BUILDER", "POLYGLOT", "WEEKEND_WARRIOR",
                    "REFACTOR_MASTER", "DOCUMENTARIAN", "TESTER", "EXPLORER", "COLLABORATOR"};
            StringBuilder gallery = new StringBuilder(EvidenceSvg.open(1264, 1152, "DevDNA / Type gallery"));
            gallery.append("<rect width=\"1264\" height=\"1152\" fill=\"#faf7f0\"/>");
            for (int i = 0; i < types.length; i++) {
                result.setDeveloperType(types[i]);
                String item = new BadgeGenerator().generateForResult(result, null, true);
                ShareSvgTestSupport.parse(item);
                Files.writeString(directory.resolve("badge-" + types[i] + ".svg"), item);
                gallery.append("<g transform=\"translate(").append(24 + i % 2 * 616)
                        .append(' ').append(24 + i / 2 * 224).append(")\">")
                        .append(item).append("</g>");
            }
            Files.writeString(directory.resolve("badge-gallery.svg"), gallery.append("</svg>").toString());
        }
    }
}
