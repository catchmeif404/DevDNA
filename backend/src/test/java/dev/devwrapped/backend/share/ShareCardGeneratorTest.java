package dev.devwrapped.backend.share;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.backend.analysis.AnalysisResult;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ShareCardGeneratorTest {
    private final ShareCardGenerator generator = new ShareCardGenerator();
    private final BadgeGenerator badge = new BadgeGenerator();

    @Test
    void rendersDossierWithRecordedCountsAndLanguage() throws Exception {
        var document = ShareSvgTestSupport.parse(generator.generate(ShareSvgTestSupport.result()));
        assertThat(document.getDocumentElement().getAttribute("viewBox")).isEqualTo("0 0 600 800");
        assertThat(document.getDocumentElement().getTextContent()).contains(
                "DEVDNA", "DEVELOPER CASE FILE", "SUBJECT / GITHUB", "@example-dev", "BUG HUNTER",
                "1,842", "24", "89", "COMMITS", "REPOSITORIES", "PULL REQUESTS", "Java", "REVIEWED",
                "TYPE EMBLEM", "You found the file. You didn't find me.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"FUTURE_TYPE", "<script>alert('type')</script>", "   "})
    void missingAndUnknownTypesUseSafeClassification(String type) throws Exception {
        AnalysisResult result = new AnalysisResult();
        result.setDeveloperType(type);
        for (String svg : new String[] {generator.generate(result), badge.generateForResult(result, 1)}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(document.getDocumentElement().getTextContent()).contains("UNCLASSIFIED", "@UNKNOWN", "UNRECORDED", "0");
            assertThat(svg).doesNotContain("FUTURE_TYPE", "<script", ">null<");
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
        assertThat(generator.generate(null)).contains("PENDING", "No public analysis on file.").doesNotContain("REVIEWED");
        ShareSvgTestSupport.parse(generator.generate(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"<script>alert(1)</script>", "\" onload=\"alert(1)", "A&B<>'\"", "a\u0000b\u0001c\uD800d\uFFFEe", "line\nbreak\tvalue"})
    void userTextRemainsTextAndAlwaysFormsValidXml(String payload) throws Exception {
        AnalysisResult result = ShareSvgTestSupport.result();
        result.setGithubUsername(payload);
        result.setTopLanguage(payload);
        for (String svg : new String[] {generator.generate(result), badge.generateForResult(result, 3)}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(document.getElementsByTagName("script").getLength()).isZero();
            var all = document.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                var attributes = all.item(i).getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    assertThat(attributes.item(j).getNodeName()).doesNotStartWith("on");
                }
            }
            assertThat(document.getDocumentElement().getTextContent())
                    .contains(EvidenceSvg.clean(payload, "UNRECORDED", 39));
            if (payload.equals("A&B<>'\"")) {
                assertThat(svg).contains("A&amp;B&lt;&gt;&apos;&quot;");
            }
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
    }

    @Test
    void longestGithubUsernameAndLargeCountsFitWithoutTruncatingIdentity() throws Exception {
        AnalysisResult result = ShareSvgTestSupport.result();
        result.setGithubUsername("a".repeat(39));
        result.setTopLanguage("Unrecognized language ".repeat(100));
        result.setTotalCommits(Integer.MAX_VALUE);
        result.setTotalRepositories(Integer.MAX_VALUE);
        result.setTotalPullRequests(Integer.MAX_VALUE);
        for (String svg : new String[] {generator.generate(result), badge.generateForResult(result, Integer.MAX_VALUE)}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(document.getDocumentElement().getTextContent()).contains("@" + "a".repeat(39), "2,147,483,647", "...");
            assertThat(svg.length()).isLessThan(12000);
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
    }

    @Test
    void boundsOversizedUnicodeAndInvalidCounts() throws Exception {
        AnalysisResult result = ShareSvgTestSupport.result();
        result.setGithubUsername("\uD83D\uDD0D".repeat(1000));
        result.setTopLanguage("\uAC00".repeat(1000));
        result.setTotalCommits(-1);
        result.setTotalRepositories(Integer.MIN_VALUE);
        for (String svg : new String[] {generator.generate(result), badge.generateForResult(result, 1)}) {
            var document = ShareSvgTestSupport.parse(svg);
            assertThat(svg).doesNotContain("-1</text>", "-2,147,483,648");
            assertThat(svg.length()).isLessThan(12000);
            ShareSvgTestSupport.assertTextWithinPage(document);
        }
    }

    @Test
    void formattingDoesNotDependOnServerLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            for (String svg : new String[] {generator.generate(ShareSvgTestSupport.result()),
                    badge.generateForResult(ShareSvgTestSupport.result(), null)}) {
                ShareSvgTestSupport.assertTextWithinPage(ShareSvgTestSupport.parse(svg));
                assertThat(svg).contains("1,842").doesNotContain("1.842");
            }
        } finally {
            Locale.setDefault(original);
        }
    }
}
