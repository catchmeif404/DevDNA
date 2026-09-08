package dev.devwrapped.backend.analyzer;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Classifies every commit message in an analysis into a {@link CommitType}, preferring whichever
 * {@link AiCommitClassifier} bean's name() matches ai.commit-classifier.provider (unset/"regex" by
 * default — an analysis never requires an Anthropic API key to run) and falling back to the plain
 * {@link CommitClassifier} regex/keyword rules per chunk on any failure. Same
 * provider-select-with-graceful-degrade shape as aws-cost-calculator's ai.ExplanationService — the
 * caller (FeatureExtractor) never sees an AI error, only a less accurate classification.
 *
 * <p>Commits are classified in chunks (not one LLM call per commit, and not the whole ~400-commit
 * batch in a single call) so one call's failure or truncated response only degrades one chunk, and
 * so an active user's full commit history still fits comfortably in one prompt/response pair.
 */
@Component
public class CommitTypeResolver {

    private static final Logger log = LoggerFactory.getLogger(CommitTypeResolver.class);
    private static final int CHUNK_SIZE = 100;

    private final List<AiCommitClassifier> aiClassifiers;
    private final CommitClassifier fallbackClassifier;
    private final String provider;

    public CommitTypeResolver(List<AiCommitClassifier> aiClassifiers, CommitClassifier fallbackClassifier,
            @Value("${ai.commit-classifier.provider:regex}") String provider) {
        this.aiClassifiers = aiClassifiers;
        this.fallbackClassifier = fallbackClassifier;
        this.provider = provider;
    }

    public List<CommitType> resolveAll(List<String> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        AiCommitClassifier primary = findProvider();
        if (primary == null) {
            return classifyWithRegex(messages);
        }

        List<CommitType> result = new ArrayList<>(messages.size());
        for (int start = 0; start < messages.size(); start += CHUNK_SIZE) {
            List<String> chunk = messages.subList(start, Math.min(start + CHUNK_SIZE, messages.size()));
            result.addAll(classifyChunk(primary, chunk));
        }
        return result;
    }

    private List<CommitType> classifyChunk(AiCommitClassifier primary, List<String> chunk) {
        try {
            List<CommitType> types = primary.classifyBatch(chunk);
            if (types != null && types.size() == chunk.size()) {
                return types;
            }
            log.warn("AI commit classifier ({}) returned {} labels for {} commits, falling back to regex for this chunk",
                    primary.name(), types == null ? -1 : types.size(), chunk.size());
        } catch (Exception e) {
            log.warn("AI commit classifier ({}) failed, falling back to regex for this chunk: {}",
                    primary.name(), e.getMessage());
        }
        return classifyWithRegex(chunk);
    }

    private List<CommitType> classifyWithRegex(List<String> messages) {
        return messages.stream().map(fallbackClassifier::classify).toList();
    }

    private AiCommitClassifier findProvider() {
        if (provider == null || provider.isBlank() || "regex".equalsIgnoreCase(provider) || "none".equalsIgnoreCase(provider)) {
            return null;
        }
        return aiClassifiers.stream()
                .filter(c -> c.name().equals(provider))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("ai.commit-classifier.provider '{}' matches no AiCommitClassifier bean; using regex classification", provider);
                    return null;
                });
    }
}
