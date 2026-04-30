package com.bookos.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.ai.enabled=true",
        "app.ai.provider=openai-compatible",
        "app.ai.openai-compatible.base-url=http://127.0.0.1:1/v1",
        "app.ai.openai-compatible.api-key=bookos-test-secret-key-never-expose",
        "app.ai.openai-compatible.model=test-model"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIProviderSecretStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void providerStatusDoesNotExposeOpenAICompatibleSecrets() throws Exception {
        String token = register();

        String response = mockMvc.perform(get("/api/ai/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.configuredProvider").value("openai-compatible"))
                .andExpect(jsonPath("$.data.externalProviderConfigured").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response)
                .doesNotContain("bookos-test-secret-key-never-expose")
                .doesNotContain("apiKey")
                .doesNotContain("OPENAI_COMPATIBLE_API_KEY")
                .doesNotContain("Authorization");
    }

    private String register() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ai-secret-%s@bookos.local",
                                  "password": "Password123!",
                                  "displayName": "AI Secret Tester"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
