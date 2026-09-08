package dev.devwrapped.backend.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommitTypeResolverTest {

    private final CommitClassifier regex = new CommitClassifier();

    @Test
    void usesRegexOnlyWhenProviderIsRegex() {
        AiCommitClassifier neverCalled = failingClassifier("claude-api");
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(neverCalled), regex, "regex");

        List<CommitType> result = resolver.resolveAll(List.of("feat: add login", "fix: bug"));

        assertThat(result).containsExactly(CommitType.FEATURE, CommitType.FIX);
    }

    @Test
    void usesRegexOnlyWhenProviderUnset() {
        AiCommitClassifier neverCalled = failingClassifier("claude-api");
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(neverCalled), regex, "");

        List<CommitType> result = resolver.resolveAll(List.of("docs: update readme"));

        assertThat(result).containsExactly(CommitType.DOCUMENTATION);
    }

    @Test
    void usesAiClassifierWhenProviderMatchesAndSizeAgrees() {
        AiCommitClassifier ai = fixedClassifier("claude-api", CommitType.REFACTOR, CommitType.TEST);
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(ai), regex, "claude-api");

        List<CommitType> result = resolver.resolveAll(List.of("wip", "wip"));

        assertThat(result).containsExactly(CommitType.REFACTOR, CommitType.TEST);
    }

    @Test
    void fallsBackToRegexWhenAiClassifierThrows() {
        AiCommitClassifier ai = failingClassifier("claude-api");
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(ai), regex, "claude-api");

        List<CommitType> result = resolver.resolveAll(List.of("fix: crash on null"));

        assertThat(result).containsExactly(CommitType.FIX);
    }

    @Test
    void fallsBackToRegexWhenAiClassifierReturnsWrongSize() {
        AiCommitClassifier ai = fixedClassifier("claude-api", CommitType.FEATURE);
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(ai), regex, "claude-api");

        List<CommitType> result = resolver.resolveAll(List.of("fix: a", "fix: b"));

        assertThat(result).containsExactly(CommitType.FIX, CommitType.FIX);
    }

    @Test
    void fallsBackToRegexWhenProviderMatchesNoBean() {
        AiCommitClassifier ai = failingClassifier("some-other-provider");
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(ai), regex, "claude-api");

        List<CommitType> result = resolver.resolveAll(List.of("test: add case"));

        assertThat(result).containsExactly(CommitType.TEST);
    }

    @Test
    void chunksLargeBatchesAndClassifiesEveryCommit() {
        AiCommitClassifier ai = new AiCommitClassifier() {
            @Override
            public String name() {
                return "claude-api";
            }

            @Override
            public List<CommitType> classifyBatch(List<String> messages) {
                List<CommitType> result = new ArrayList<>();
                for (int i = 0; i < messages.size(); i++) {
                    result.add(CommitType.OTHER);
                }
                return result;
            }
        };
        CommitTypeResolver resolver = new CommitTypeResolver(List.of(ai), regex, "claude-api");

        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 250; i++) {
            messages.add("wip " + i);
        }

        List<CommitType> result = resolver.resolveAll(messages);

        assertThat(result).hasSize(250);
        assertThat(result).allMatch(type -> type == CommitType.OTHER);
    }

    private AiCommitClassifier fixedClassifier(String name, CommitType... types) {
        return new AiCommitClassifier() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<CommitType> classifyBatch(List<String> messages) {
                return List.of(types);
            }
        };
    }

    private AiCommitClassifier failingClassifier(String name) {
        return new AiCommitClassifier() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<CommitType> classifyBatch(List<String> messages) {
                throw new RuntimeException("boom");
            }
        };
    }
}
