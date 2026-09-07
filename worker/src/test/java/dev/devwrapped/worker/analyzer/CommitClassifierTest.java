package dev.devwrapped.worker.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CommitClassifierTest {

    private final CommitClassifier classifier = new CommitClassifier();

    @Test
    void classifiesConventionalPrefixes() {
        assertThat(classifier.classify("feat: add oauth login")).isEqualTo(CommitType.FEATURE);
        assertThat(classifier.classify("fix: resolve login bug")).isEqualTo(CommitType.FIX);
        assertThat(classifier.classify("refactor: simplify user service")).isEqualTo(CommitType.REFACTOR);
        assertThat(classifier.classify("docs: update README")).isEqualTo(CommitType.DOCUMENTATION);
        assertThat(classifier.classify("test: add login test")).isEqualTo(CommitType.TEST);
    }

    @Test
    void classifiesByKeywordWithoutConventionalPrefix() {
        assertThat(classifier.classify("Implement retry logic for worker")).isEqualTo(CommitType.FEATURE);
        assertThat(classifier.classify("Hotfix for null pointer in scoring")).isEqualTo(CommitType.FIX);
        assertThat(classifier.classify("Cleanup unused imports")).isEqualTo(CommitType.REFACTOR);
    }

    @Test
    void fixTakesPrecedenceOverFeatureKeyword() {
        assertThat(classifier.classify("fix: implement missing null check")).isEqualTo(CommitType.FIX);
    }

    @Test
    void defaultsToOtherWhenNoRuleMatches() {
        assertThat(classifier.classify("wip")).isEqualTo(CommitType.OTHER);
        assertThat(classifier.classify(null)).isEqualTo(CommitType.OTHER);
        assertThat(classifier.classify("")).isEqualTo(CommitType.OTHER);
    }
}
