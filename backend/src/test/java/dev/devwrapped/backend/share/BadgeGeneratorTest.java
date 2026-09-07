package dev.devwrapped.backend.share;

import static org.assertj.core.api.Assertions.assertThat;

import dev.devwrapped.backend.analysis.AnalysisResult;
import org.junit.jupiter.api.Test;

class BadgeGeneratorTest {

    private final BadgeGenerator generator = new BadgeGenerator();

    @Test
    void rendersAnimatedAnimalBadgeWithoutEmoji() {
        AnalysisResult result = new AnalysisResult();
        result.setDeveloperType("BUG_SLAYER");
        result.setTotalCommits(1842);
        result.setTopLanguage("Java");

        String svg = generator.generateForResult(result);

        assertThat(svg).contains("aria-label=\"DevDNA: DEBUG CAT");
        assertThat(svg).contains("<animate ");
        assertThat(svg).contains("DEVDNA");
        assertThat(svg).contains("DEBUG CAT");
        assertThat(svg).contains("Bug-hunting Java mode");
        assertThat(svg).contains("1.8k commits");
        assertThat(svg).contains("#dc2626");
        assertThat(svg).contains("rotate");
        assertThat(svg).doesNotContain("🔥");
    }

    @Test
    void rendersAnimatedNoDataBadge() {
        String svg = generator.generateNoData();

        assertThat(svg).contains("aria-label=\"DevDNA: no data yet");
        assertThat(svg).contains("<animate ");
        assertThat(svg).contains("no data yet");
        assertThat(svg).contains("#6b7280");
    }

    @Test
    void rendersCustomPenguinMascotForCollaborator() {
        AnalysisResult result = new AnalysisResult();
        result.setDeveloperType("COLLABORATOR");
        result.setTotalCommits(320);
        result.setTotalPullRequests(89);
        result.setTopLanguage("Scala");

        String svg = generator.generateForResult(result);

        assertThat(svg).contains("TEAM PENGUIN");
        assertThat(svg).contains("Team-powered maker");
        assertThat(svg).contains("89 PRs");
        assertThat(svg).contains("#fb7185");
        assertThat(svg).contains("type=\"scale\"");
    }
}
