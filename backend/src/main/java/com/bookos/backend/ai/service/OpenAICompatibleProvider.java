package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenAICompatibleProvider implements AIProvider {

    private final AIProviderProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAICompatibleProvider(AIProviderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "OpenAICompatibleProvider";
    }

    @Override
    public boolean available() {
        AIProviderProperties.OpenAICompatible config = properties.getOpenaiCompatible();
        return StringUtils.hasText(config.getBaseUrl())
                && StringUtils.hasText(config.getApiKey())
                && StringUtils.hasText(config.getModel());
    }

    @Override
    public AIDraft generate(AISuggestionType type, String sourceText, String sourceTitle) {
        if (!available()) {
            throw new AIProviderUnavailableException("OpenAI-compatible provider is not configured.");
        }

        String content = requestJsonDraft(type, sourceText, sourceTitle);
        JsonNode root = parseProviderJson(content);
        return new AIDraft(draftText(type, root), root.toString());
    }

    private String requestJsonDraft(AISuggestionType type, String sourceText, String sourceTitle) {
        AIProviderProperties.OpenAICompatible config = properties.getOpenaiCompatible();
        String baseUrl = trimTrailingSlash(config.getBaseUrl());
        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(config.getTimeoutSeconds()))
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        Map<String, Object> request = Map.of(
                "model", config.getModel(),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt(type)),
                        Map.of("role", "user", "content", userPrompt(sourceText, sourceTitle))));

        try {
            JsonNode response = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            String content = response == null
                    ? null
                    : response.path("choices").path(0).path("message").path("content").asText(null);
            if (!StringUtils.hasText(content)) {
                throw new AIProviderUnavailableException("OpenAI-compatible provider returned an empty response.");
            }
            return content;
        } catch (RestClientException exception) {
            throw new AIProviderUnavailableException("OpenAI-compatible provider request failed or timed out.");
        }
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        int seconds = Math.max(1, timeoutSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(seconds));
        factory.setReadTimeout(Duration.ofSeconds(seconds));
        return factory;
    }

    private JsonNode parseProviderJson(String content) {
        String cleaned = stripJsonFence(content);
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception exception) {
            throw new IllegalArgumentException("OpenAI-compatible provider returned malformed JSON.");
        }
    }

    private String stripJsonFence(String value) {
        String cleaned = value.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return cleaned;
    }

    private String systemPrompt(AISuggestionType type) {
        return """
                You are BookOS draft assistant. Return JSON only.
                Never overwrite user content. Never include secrets. Never invent page numbers.
                Use only the source text provided by the user message.
                Always include: provider, type, sourceTitle.
                Task-specific schema: %s
                """.formatted(schemaFor(type));
    }

    private String schemaFor(AISuggestionType type) {
        return switch (type) {
            case NOTE_SUMMARY -> """
                    {"provider":"OpenAICompatibleProvider","type":"NOTE_SUMMARY","sourceTitle":"...","summary":"short draft summary"}
                    """;
            case EXTRACT_ACTIONS -> """
                    {"provider":"OpenAICompatibleProvider","type":"EXTRACT_ACTIONS","sourceTitle":"...","actions":[{"title":"...","priority":"LOW|MEDIUM|HIGH"}]}
                    """;
            case EXTRACT_CONCEPTS -> """
                    {"provider":"OpenAICompatibleProvider","type":"EXTRACT_CONCEPTS","sourceTitle":"...","concepts":["Concept Name"]}
                    """;
            case SUGGEST_DESIGN_LENSES -> """
                    {"provider":"OpenAICompatibleProvider","type":"SUGGEST_DESIGN_LENSES","sourceTitle":"...","lenses":[{"name":"Lens Name","question":"Diagnostic question"}]}
                    """;
            case SUGGEST_PROJECT_APPLICATIONS -> """
                    {"provider":"OpenAICompatibleProvider","type":"SUGGEST_PROJECT_APPLICATIONS","sourceTitle":"...","applications":[{"title":"...","applicationType":"PROJECT_APPLICATION","description":"..."}]}
                    """;
            case FORUM_THREAD_DRAFT -> """
                    {"provider":"OpenAICompatibleProvider","type":"FORUM_THREAD_DRAFT","sourceTitle":"...","title":"...","bodyMarkdown":"..."}
                    """;
        };
    }

    private String userPrompt(String sourceText, String sourceTitle) {
        return """
                Source title: %s
                Source text:
                %s
                """.formatted(StringUtils.hasText(sourceTitle) ? sourceTitle : "Selected BookOS source",
                StringUtils.hasText(sourceText) ? sourceText : "No source text available.");
    }

    private String draftText(AISuggestionType type, JsonNode root) {
        return switch (type) {
            case NOTE_SUMMARY -> root.path("summary").asText("");
            case EXTRACT_ACTIONS -> firstArrayText(root.path("actions"), "title", "Draft action items");
            case EXTRACT_CONCEPTS -> "Draft concepts: " + firstStringArray(root.path("concepts"));
            case SUGGEST_DESIGN_LENSES -> firstArrayText(root.path("lenses"), "name", "Draft design lenses");
            case SUGGEST_PROJECT_APPLICATIONS -> firstArrayText(root.path("applications"), "title", "Draft project applications");
            case FORUM_THREAD_DRAFT -> root.path("title").asText("Draft forum thread") + "\n\n" + root.path("bodyMarkdown").asText("");
        };
    }

    private String firstArrayText(JsonNode array, String field, String fallback) {
        if (!array.isArray() || array.isEmpty()) {
            return fallback;
        }
        return array.path(0).path(field).asText(fallback);
    }

    private String firstStringArray(JsonNode array) {
        if (!array.isArray() || array.isEmpty()) {
            return "No concepts";
        }
        java.util.List<String> values = new java.util.ArrayList<>();
        array.forEach(item -> values.add(item.asText()));
        return String.join(", ", values);
    }

    private String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
