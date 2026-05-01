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

        createBook(token, "Use Case Detection Book");

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys[0]").value("add-book"))
                .andExpect(jsonPath("$.data.effectiveCompletedStepKeys[0]").value("add-book"))
                .andExpect(jsonPath("$.data.stepVerification['add-book'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['capture'].state").value("blocked"));
    }

    @Test
    void readerChecklistAutoDetectsLibraryStatusAndProgress() throws Exception {
        String token = register("reader-auto");
        long bookId = createBook(token, "Reader Detection Book");
        long userBookId = addToLibrary(token, bookId);

        mockMvc.perform(get("/api/use-cases/progress/track-book-start-to-finish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stepVerification['create-book'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['add-library'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['set-reading'].state").value("blocked"))
                .andExpect(jsonPath("$.data.stepVerification['update-progress'].state").value("blocked"));

        mockMvc.perform(put("/api/user-books/" + userBookId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CURRENTLY_READING\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/user-books/" + userBookId + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"progressPercent\": 25}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/use-cases/progress/reader-mode-track-book")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys.length()").value(4))
                .andExpect(jsonPath("$.data.stepVerification['set-reading'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['update-progress'].state").value("auto"));
    }

    @Test
    void autoDetectionCoversCaptureQuoteAndProjectApplicationSteps() throws Exception {
        String token = register("workflow-auto");
        long bookId = createBook(token, "Workflow Detection Book");
        addToLibrary(token, bookId);
        long captureId = createCapture(token, bookId, "Quote marker: Source-backed original thought #test [[Loop]]");

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stepVerification['capture'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['process'].state").value("blocked"));

        mockMvc.perform(post("/api/captures/" + captureId + "/convert")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\": \"QUOTE\"}"))
                .andExpect(status().isOk());

        long projectId = createProject(token, "Use Case Detection Project");
        createProjectApplication(token, projectId);

        mockMvc.perform(get("/api/use-cases/progress/apply-quote-to-game-project")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stepVerification['quote'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['project'].state").value("auto"))
                .andExpect(jsonPath("$.data.stepVerification['project-application'].state").value("auto"));
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

    @Test
    void demoWorkspaceRecordsDoNotSatisfyNormalUseCaseVerification() throws Exception {
        String token = register("demo-excluded");

        mockMvc.perform(post("/api/demo/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/use-cases/progress/first-15-minutes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.automaticCompletedStepKeys").isEmpty())
                .andExpect(jsonPath("$.data.stepVerification['add-book'].state").value("blocked"));

        mockMvc.perform(get("/api/use-cases/progress/apply-quote-to-game-project")
                        .header("Authorization", "Bearer " + token))
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

    private long createBook(String token, String title) throws Exception {
        String body = """
                {
                  "title": "%s",
                  "authors": ["BookOS QA"],
                  "visibility": "PRIVATE"
                }
                """.formatted(title);

        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private long addToLibrary(String token, long bookId) throws Exception {
        String response = mockMvc.perform(post("/api/books/" + bookId + "/add-to-library")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "readingStatus": "BACKLOG",
                                  "readingFormat": "PHYSICAL",
                                  "ownershipStatus": "OWNED"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private long createCapture(String token, long bookId, String rawText) throws Exception {
        String body = """
                {
                  "bookId": %d,
                  "rawText": "%s"
                }
                """.formatted(bookId, rawText);

        String response = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private long createProject(String token, String title) throws Exception {
        String body = """
                {
                  "title": "%s",
                  "description": "Use case detector project",
                  "genre": "Puzzle",
                  "platform": "Web",
                  "stage": "PROTOTYPE",
                  "visibility": "PRIVATE",
                  "progressPercent": 0
                }
                """.formatted(title);

        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void createProjectApplication(String token, long projectId) throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicationType": "PROJECT_APPLICATION",
                                  "title": "Apply source-backed insight",
                                  "description": "Use case detector application",
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isCreated());
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
