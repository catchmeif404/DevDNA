package dev.devwrapped.backend.analyzer;

import dev.devwrapped.backend.ai.ClaudeApiClient;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link AiCommitClassifier} backed by a real Anthropic API call. Still only ever produces one of
 * the fixed {@link CommitType} values — the model classifies, it doesn't invent new categories or
 * compute any statistic itself (section 14 of docs/설계문서.md: AI never does core data
 * computation). Select it with ai.commit-classifier.provider=claude-api.
 */
@Component
public class ClaudeAiCommitClassifier implements AiCommitClassifier {

    private final ClaudeApiClient claudeApiClient;
    private final ObjectMapper objectMapper;

    public ClaudeAiCommitClassifier(ClaudeApiClient claudeApiClient, ObjectMapper objectMapper) {
        this.claudeApiClient = claudeApiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "claude-api";
    }

    @Override
    public List<CommitType> classifyBatch(List<String> messages) {
        String prompt = CommitClassificationPrompt.build(messages);
        String responseText = claudeApiClient.complete(prompt, CommitClassificationPrompt.maxTokensFor(messages));
        return CommitClassificationPrompt.parse(responseText, messages.size(), objectMapper);
    }
}
