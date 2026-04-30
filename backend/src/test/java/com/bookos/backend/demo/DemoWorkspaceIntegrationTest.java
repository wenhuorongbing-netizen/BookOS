package com.bookos.backend.demo;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoWorkspaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void demoWorkspaceIsUserScopedResettableAndExcludedFromNormalAnalytics() throws Exception {
        String ownerToken = register("demo-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Demo Owner");
        String intruderToken = register("demo-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Demo Intruder");

        mockMvc.perform(get("/api/demo/status").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(post("/api/demo/start").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.bookId").isNumber())
                .andExpect(jsonPath("$.data.projectId").isNumber())
                .andExpect(jsonPath("$.data.quoteId").isNumber())
                .andExpect(jsonPath("$.data.actionItemId").isNumber())
                .andExpect(jsonPath("$.data.conceptIds.length()").value(4))
                .andExpect(jsonPath("$.data.recordCounts.BOOK").value(1))
                .andExpect(jsonPath("$.data.safetyNote").value("Demo mode uses original BookOS sample content, stores unknown pages as null, and labels every created record as demo."));

        mockMvc.perform(get("/api/demo/status").header("Authorization", bearer(intruderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/analytics/reading").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(0))
                .andExpect(jsonPath("$.data.quotesCount").value(0))
                .andExpect(jsonPath("$.data.projectApplicationsCount").value(0));

        mockMvc.perform(get("/api/analytics/reading")
                        .queryParam("includeDemo", "true")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(1))
                .andExpect(jsonPath("$.data.quotesCount").value(1))
                .andExpect(jsonPath("$.data.projectApplicationsCount").value(1));

        mockMvc.perform(get("/api/analytics/knowledge").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conceptsCount").value(0))
                .andExpect(jsonPath("$.data.knowledgeObjectsCount").value(0));

        mockMvc.perform(get("/api/analytics/knowledge")
                        .queryParam("includeDemo", "true")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conceptsCount").value(4))
                .andExpect(jsonPath("$.data.knowledgeObjectsCount").value(2));

        mockMvc.perform(post("/api/demo/reset").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.recordCounts.RAW_CAPTURE", greaterThanOrEqualTo(4)));

        mockMvc.perform(delete("/api/demo").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/demo/status").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/analytics/reading")
                        .queryParam("includeDemo", "true")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(0));
    }

    private String register(String email, String displayName) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!",
                                  "displayName": "%s"
                                }
                                """.formatted(email, displayName)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
