package dev.devwrapped.backend.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Thin client for the Anthropic Messages API, configured via ai.claude.* / ANTHROPIC_* (see
 * application.yml). Mirrors aws-cost-calculator's backend.ai.ClaudeApiClient rather than
 * introducing a new HTTP client abstraction for this repo.
 */
@Component
public class ClaudeApiClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final long timeoutSeconds;

    public ClaudeApiClient(ObjectMapper objectMapper,
            @Value("${ai.claude.api-key:}") String apiKey,
            @Value("${ai.claude.model:claude-haiku-4-5-20251001}") String model,
            @Value("${ai.claude.timeout-seconds:60}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String complete(String userPrompt, int maxTokens) {
        if (!isConfigured()) {
            throw new AiClassificationException("Claude API key is not configured (ANTHROPIC_API_KEY)");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiClassificationException("Claude API HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("content");
            if (!content.isArray() || content.isEmpty()) {
                throw new AiClassificationException("Claude API response had no content");
            }
            String text = content.get(0).path("text").asString("");
            if (text == null || text.isBlank()) {
                throw new AiClassificationException("Claude API returned empty text");
            }
            return text.trim();
        } catch (AiClassificationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiClassificationException("Claude API call failed: " + e.getMessage(), e);
        }
    }
}
