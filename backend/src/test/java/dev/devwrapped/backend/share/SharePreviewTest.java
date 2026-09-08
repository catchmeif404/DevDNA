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
        }
    }
}
