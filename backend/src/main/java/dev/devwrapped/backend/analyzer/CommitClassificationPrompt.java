package dev.devwrapped.backend.analyzer;

import dev.devwrapped.backend.ai.AiClassificationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Prompt building and response parsing shared by every {@link AiCommitClassifier} implementation
 * (currently {@link ClaudeAiCommitClassifier}, {@link GeminiAiCommitClassifier}) — only the
 * underlying provider's {@code complete(prompt, maxTokens)} call differs between them.
 */
final class CommitClassificationPrompt {

    private static final int TOKENS_PER_MESSAGE_ESTIMATE = 12;
    private static final int BASE_MAX_TOKENS = 200;

    private CommitClassificationPrompt() {
    }

    static int maxTokensFor(List<String> messages) {
        return BASE_MAX_TOKENS + messages.size() * TOKENS_PER_MESSAGE_ESTIMATE;
    }

    static String build(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Classify each git commit message below into exactly one of these categories: ");
        sb.append("FEATURE, FIX, REFACTOR, DOCUMENTATION, TEST, OTHER.\n\n");
        sb.append("Use the commit's actual intent, not just a Conventional Commits prefix ")
                .append("(e.g. a message with no prefix that clearly adds a new capability is still FEATURE).\n\n");
        sb.append("Respond with ONLY a JSON array of ").append(messages.size())
                .append(" strings, one category per commit message in the same order, no other text.\n\n");
        sb.append("Commit messages:\n");
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            String firstLine = (message == null || message.isBlank())
                    ? "(no commit message)"
                    : message.strip().lines().findFirst().orElse("(no commit message)");
            sb.append(i + 1).append(". ").append(firstLine.replace("\n", " ")).append('\n');
        }
        return sb.toString();
    }

    static List<CommitType> parse(String responseText, int expectedSize, ObjectMapper objectMapper) {
        String json = extractJsonArray(responseText);
        JsonNode array;
        try {
            array = objectMapper.readTree(json);
        } catch (Exception e) {
            throw new AiClassificationException("AI commit classification returned unparseable JSON: " + e.getMessage(), e);
        }
        if (!array.isArray() || array.size() != expectedSize) {
            throw new AiClassificationException("AI commit classification returned " + array.size()
                    + " labels for " + expectedSize + " commits");
        }
        List<CommitType> types = new ArrayList<>(expectedSize);
        for (JsonNode node : array) {
            types.add(toCommitType(node.asString("")));
        }
        return types;
    }

    private static CommitType toCommitType(String raw) {
        try {
            return CommitType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return CommitType.OTHER;
        }
    }

    /** The model can wrap the array in prose or a ```json fence despite instructions not to. */
    private static String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end < start) {
            throw new AiClassificationException("AI commit classification response had no JSON array: " + text);
        }
        return text.substring(start, end + 1);
    }
}
