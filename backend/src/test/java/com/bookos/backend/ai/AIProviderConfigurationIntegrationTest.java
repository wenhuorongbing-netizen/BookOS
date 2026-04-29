package com.bookos.backend.ai;

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
        "app.ai.openai-compatible.api-key=",
        "app.ai.openai-compatible.model=test-model"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIProviderConfigurationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void missingOpenAICompatibleKeyReturnsSafeUnavailableStatus() throws Exception {
        String token = register();

        mockMvc.perform(get("/api/ai/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.configuredProvider").value("openai-compatible"))
                .andExpect(jsonPath("$.data.externalProviderConfigured").value(false));

        mockMvc.perform(post("/api/ai/suggestions/note-summary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Safe source text."
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("OpenAI-compatible provider is selected but base URL, model, or API key is missing."));
    }

    private String register() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ai-provider-%s@bookos.local",
                                  "password": "Password123!",
                                  "displayName": "AI Provider Tester"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
