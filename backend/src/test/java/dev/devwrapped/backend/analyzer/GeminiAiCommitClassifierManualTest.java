package dev.devwrapped.backend.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.devwrapped.backend.ai.GeminiApiClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Hits the real Gemini API — skipped (not failed) unless a key is available via the GEMINI_API_KEY
 * env var or a local `backend/.env` (gitignored, never read by this class into anything but the
 * test JVM's own process). Exists to verify GeminiApiClient's request/response handling against
 * the live API once; CommitTypeResolverTest already covers fallback/chunking logic with fakes, so
 * this isn't meant to run in CI.
 */
class GeminiAiCommitClassifierManualTest {

    @Test
    void classifiesRealCommitMessagesViaGemini() {
        String apiKey = resolveGeminiApiKey();
        assumeTrue(apiKey != null && !apiKey.isBlank(),
                "GEMINI_API_KEY not set (env var or backend/.env) - skipping live Gemini call");

        ObjectMapper objectMapper = new ObjectMapper();
        GeminiApiClient client = new GeminiApiClient(objectMapper, apiKey, "gemini-3.5-flash-lite", 60);
        GeminiAiCommitClassifier classifier = new GeminiAiCommitClassifier(client, objectMapper);

        List<String> messages = List.of(
                "feat: add oauth login flow",
                "fix: null pointer when repo has no language",
                "refactor: extract CollectorService retry logic",
                "docs: document the async analysis pipeline",
                "add unit tests for DeveloperTypeScorer",
                "quick wip commit, forgot what this does");

        List<CommitType> result = classifier.classifyBatch(messages);

        assertThat(result).hasSize(messages.size());
        assertThat(result.get(0)).isEqualTo(CommitType.FEATURE);
        assertThat(result.get(1)).isEqualTo(CommitType.FIX);
        assertThat(result.get(2)).isEqualTo(CommitType.REFACTOR);
        assertThat(result.get(3)).isEqualTo(CommitType.DOCUMENTATION);
        System.out.println("Gemini classification result: " + result);
    }

    private String resolveGeminiApiKey() {
        String fromEnv = System.getenv("GEMINI_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return readFromDotEnv().orElse(null);
    }

    private Optional<String> readFromDotEnv() {
        Path dotEnv = Path.of(System.getProperty("user.dir"), ".env");
        if (!Files.exists(dotEnv)) {
            return Optional.empty();
        }
        try {
            return Files.readAllLines(dotEnv, StandardCharsets.UTF_8).stream()
                    .map(String::strip)
                    .filter(line -> line.startsWith("GEMINI_API_KEY="))
                    .map(line -> line.substring("GEMINI_API_KEY=".length()).strip())
                    .filter(value -> !value.isBlank())
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
