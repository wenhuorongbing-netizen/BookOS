package com.bookos.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bookos.backend.ai.service.AIDraft;
import com.bookos.backend.ai.service.AIProviderProperties;
import com.bookos.backend.ai.service.AISuggestionValidator;
import com.bookos.backend.ai.service.OpenAICompatibleProvider;
import com.bookos.backend.common.enums.AISuggestionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AIProviderServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validatorRejectsMalformedJsonAndInventedPages() {
        AISuggestionValidator validator = new AISuggestionValidator(objectMapper);

        assertThatThrownBy(() -> validator.validate(AISuggestionType.NOTE_SUMMARY, new AIDraft("Draft", "not-json"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");

        assertThatThrownBy(() -> validator.validate(
                        AISuggestionType.NOTE_SUMMARY,
                        new AIDraft("Draft", """
                                {
                                  "provider": "MockAIProvider",
                                  "type": "NOTE_SUMMARY",
                                  "summary": "Draft",
                                  "pageStart": 42
                                }
                                """),
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invent page numbers");
    }

    @Test
    void validatorRejectsWrongSuggestionTypeAndOverwriteTargets() {
        AISuggestionValidator validator = new AISuggestionValidator(objectMapper);

        assertThatThrownBy(() -> validator.validate(
                        AISuggestionType.NOTE_SUMMARY,
                        new AIDraft("Draft", """
                                {
                                  "provider": "MockAIProvider",
                                  "type": "EXTRACT_CONCEPTS",
                                  "summary": "Wrong type"
                                }
                                """),
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type must match");

        assertThatThrownBy(() -> validator.validate(
                        AISuggestionType.NOTE_SUMMARY,
                        new AIDraft("Draft", """
                                {
                                  "provider": "MockAIProvider",
                                  "type": "NOTE_SUMMARY",
                                  "summary": "Unsafe overwrite request",
                                  "nested": {
                                    "targetEntityId": 99
                                  }
                                }
                                """),
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overwrite");
    }

    @Test
    void openAICompatibleProviderUsesMockedServerAndReturnsJsonDraft() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = """
                    {
                      "choices": [
                        {
                          "message": {
                            "content": "{\\"provider\\":\\"OpenAICompatibleProvider\\",\\"type\\":\\"EXTRACT_CONCEPTS\\",\\"sourceTitle\\":\\"Test Source\\",\\"concepts\\":[\\"Core Loop\\"]}"
                          }
                        }
                      ]
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            AIProviderProperties properties = new AIProviderProperties();
            properties.getOpenaiCompatible().setBaseUrl("http://127.0.0.1:%d/v1".formatted(server.getAddress().getPort()));
            properties.getOpenaiCompatible().setApiKey("test-key");
            properties.getOpenaiCompatible().setModel("test-model");
            properties.getOpenaiCompatible().setTimeoutSeconds(2);

            OpenAICompatibleProvider provider = new OpenAICompatibleProvider(properties, objectMapper);
            AIDraft draft = provider.generate(AISuggestionType.EXTRACT_CONCEPTS, "A source about core loops.", "Test Source");

            assertThat(draft.draftText()).contains("Core Loop");
            assertThat(objectMapper.readTree(draft.draftJson()).path("concepts").get(0).asText()).isEqualTo("Core Loop");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void openAICompatibleProviderFailsSafeWhenKeyMissing() {
        AIProviderProperties properties = new AIProviderProperties();
        properties.getOpenaiCompatible().setBaseUrl("http://127.0.0.1:1/v1");
        properties.getOpenaiCompatible().setApiKey("");
        properties.getOpenaiCompatible().setModel("test-model");

        OpenAICompatibleProvider provider = new OpenAICompatibleProvider(properties, objectMapper);

        assertThat(provider.available()).isFalse();
        assertThatThrownBy(() -> provider.generate(AISuggestionType.NOTE_SUMMARY, "source", "title"))
                .hasMessageContaining("not configured");
    }
}
