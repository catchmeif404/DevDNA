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
 * Thin client for the Google Gemini "generateContent" API, configured via ai.gemini.* / GEMINI_*
 * (see application.yml). A free API key (no credit card, generous free-tier limits) is available
 * at https://aistudio.google.com/apikey. Mirrors aws-cost-calculator's backend.ai.GeminiApiClient.
 */
@Component
public class GeminiApiClient {

    private static final String API_VERSION = "v1beta";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final long timeoutSeconds;

    public GeminiApiClient(ObjectMapper objectMapper,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-3.5-flash-lite}") String model,
            @Value("${ai.gemini.timeout-seconds:60}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String complete(String userPrompt, int maxOutputTokens) {
        if (!isConfigured()) {
            throw new AiClassificationException("Gemini API key is not configured (GEMINI_API_KEY)");
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt))
                )),
                "generationConfig", Map.of("maxOutputTokens", maxOutputTokens)
        );

        String url = "https://generativelanguage.googleapis.com/" + API_VERSION
                + "/models/" + model + ":generateContent";

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("x-goog-api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiClassificationException("Gemini API HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiClassificationException("Gemini API response had no candidates");
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new AiClassificationException("Gemini API candidate had no content parts");
            }
            StringBuilder text = new StringBuilder();
            for (JsonNode part : parts) {
                text.append(part.path("text").asString(""));
            }
            if (text.isEmpty()) {
                throw new AiClassificationException("Gemini API returned empty text");
            }
            return text.toString().trim();
        } catch (AiClassificationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiClassificationException("Gemini API call failed: " + e.getMessage(), e);
        }
    }
}
