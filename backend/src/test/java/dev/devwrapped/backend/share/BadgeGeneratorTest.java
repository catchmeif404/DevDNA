package dev.devwrapped.backend.share;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BadgeGeneratorTest {
    private final BadgeGenerator generator = new BadgeGenerator();

    @ParameterizedTest
    @CsvSource({
            "NIGHT_OWL, AFTER-HOURS OPERATOR", "BUG_SLAYER, BUG HUNTER", "BUILDER, SERIAL BUILDER",
            "POLYGLOT, MULTILINGUAL OPERATOR", "WEEKEND_WARRIOR, WEEKEND OPERATIVE",
            "REFACTOR_MASTER, CODE RESTORER", "DOCUMENTARIAN, ARCHIVIST", "TESTER, QUALITY INSPECTOR",
            "EXPLORER, REPO EXPLORER", "COLLABORATOR, COLLABORATOR"
    })
    void rendersEveryTypeAsValidStaticEvidence(String type, String label) throws Exception {
        AnalysisResult result = ShareSvgTestSupport.result();
        result.setDeveloperType(type);
        String badge = generator.generateForResult(result, null);
        String card = new ShareCardGenerator().generate(result);

        for (String svg : new String[] {badge, card}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(document.getDocumentElement().getTextContent()).contains(label, "@example-dev", "REVIEWED");
            assertThat(svg).contains("#e8dcc3", "#f1e9d2", "#241f1a", "#a32b2b", "Courier Prime", "Courier New");
            assertThat(svg).doesNotContain("<animate", "Gradient", "<script", "<image", "<foreignObject", "SAMPLE");
            assertThat(document.getElementsByTagName("path").getLength()).isPositive();
            assertThat(svg).doesNotContain("M8 8 L44 44", "M44 8 L8 44");
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
        assertThat(badge).contains("1,842 commits / 24 repos / 89 PRs", "LANGUAGE: Java");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, Integer.MAX_VALUE})
    void founderDistinctionIsVisibleAndAccessible(int rank) throws Exception {
        var document = ShareSvgTestSupport.parse(generator.generateForResult(ShareSvgTestSupport.result(), rank));
        assertThat(document.getDocumentElement().getAttribute("aria-label")).contains("Founding member #" + rank);
        assertThat(document.getElementsByTagName("text").item(
                document.getElementsByTagName("text").getLength() - 1).getTextContent())
                .isEqualTo("Founding member #" + rank);
        ShareSvgTestSupport.assertTextWithinPage(document);
    }

    @Test
    void omitsFounderDistinctionWhenAbsentOrInvalid() throws Exception {
        for (Integer rank : new Integer[] {null, 0, -1}) {
            String svg = generator.generateForResult(ShareSvgTestSupport.result(), rank);
            ShareSvgTestSupport.parse(svg);
            assertThat(svg).doesNotContain("Founding member");
        }
    }

    @Test
    void noDataHasAnExplicitPendingState() throws Exception {
        String svg = generator.generateNoData();
        var document = ShareSvgTestSupport.parse(svg);
        assertThat(document.getDocumentElement().getTextContent())
                .contains("AWAITING EVIDENCE", "PENDING", "no data yet", "No public analysis on file.");
        assertThat(svg).doesNotContain("REVIEWED", "Founding member", "<animate");
        assertThat(generator.generateForResult(null, 2)).isEqualTo(svg);
        ShareSvgTestSupport.assertTextWithinPage(document);
    }
}
