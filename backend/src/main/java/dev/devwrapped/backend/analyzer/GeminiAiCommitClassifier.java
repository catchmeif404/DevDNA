package dev.devwrapped.backend.analyzer;

import dev.devwrapped.backend.ai.GeminiApiClient;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link AiCommitClassifier} backed by a real Google Gemini API call — same contract and prompt as
 * {@link ClaudeAiCommitClassifier}, just a different provider. Select it with
 * ai.commit-classifier.provider=gemini-api.
 */
@Component
public class GeminiAiCommitClassifier implements AiCommitClassifier {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    public GeminiAiCommitClassifier(GeminiApiClient geminiApiClient, ObjectMapper objectMapper) {
        this.geminiApiClient = geminiApiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "gemini-api";
    }

    @Override
    public List<CommitType> classifyBatch(List<String> messages) {
        String prompt = CommitClassificationPrompt.build(messages);
        String responseText = geminiApiClient.complete(prompt, CommitClassificationPrompt.maxTokensFor(messages));
        return CommitClassificationPrompt.parse(responseText, messages.size(), objectMapper);
    }
}
