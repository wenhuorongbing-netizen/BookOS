package com.bookos.backend.usecase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class UseCaseProgressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void progressIsUserScopedAndCanBeReset() throws Exception {
        String ownerToken = register("owner");
        String otherToken = register("other");

        mockMvc.perform(post("/api/use-cases/progress/first-15-minutes/start")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(put("/api/use-cases/progress/first-15-minutes/steps/open-source/complete")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedStepKeys[0]").value("open-source"));

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.completedStepKeys").isEmpty());

        mockMvc.perform(put("/api/use-cases/progress/first-15-minutes/reset")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.completedStepKeys").isEmpty());
    }

    @Test
    void automaticDetectionUsesRealOwnedData() throws Exception {
        String token = register("auto");

        String bookBody = """
                {
                  "title": "Use Case Detection Book",
                  "authors": ["BookOS QA"],
                  "visibility": "PRIVATE"
                }
                """;

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[0]").value("add-book"))
                .andExpect(jsonPath("$.data.effectiveCompletedStepKeys[0]").value("add-book"));
    }

    @Test
    void routeEventsDriveAdvancedChecklistDetectionWithoutLeakingAcrossUsers() throws Exception {
        String ownerToken = register("event-owner");
        String otherToken = register("event-other");

        recordEvent(ownerToken, "SOURCE_OPENED", "QUOTE", "42");
        recordEvent(ownerToken, "SEARCH_USED", "GLOBAL_SEARCH", "core-loop");
        recordEvent(ownerToken, "GRAPH_OPENED", "GRAPH", "workspace");
        recordEvent(ownerToken, "EXPORT_STARTED", "BOOKOS_JSON", "all");

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys").isArray())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[0]").value("open-source"));

        mockMvc.perform(get("/api/use-cases/progress/advanced-mode-search-graph-export-ai")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[0]").value("search"))
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[1]").value("graph"))
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[2]").value("export"));

        mockMvc.perform(get("/api/use-cases/progress/advanced-mode-search-graph-export-ai")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys").isEmpty());
    }

    private void recordEvent(String token, String eventType, String contextType, String contextId) throws Exception {
        String body = """
                {
                  "eventType": "%s",
                  "contextType": "%s",
                  "contextId": "%s"
                }
                """.formatted(eventType, contextType, contextId);

        mockMvc.perform(post("/api/use-cases/progress/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventType").value(eventType));
    }

    private String register(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@bookos.local";
        String body = """
                {
                  "email": "%s",
                  "password": "Password123!",
                  "displayName": "Use Case %s"
                }
                """.formatted(email, prefix);

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("token").asText();
    }
}
