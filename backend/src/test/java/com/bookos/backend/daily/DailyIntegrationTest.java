package com.bookos.backend.daily;

import static org.hamcrest.Matchers.not;
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
class DailyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dailySelectionIsStableCanRegenerateAndPreservesSourceReferences() throws Exception {
        String token = loginAsDesigner();
        BookLibraryIds ids = createBookAndAddToLibrary(token);

        createQuoteCapture(token, ids.bookId(), 112, "The jump arc communicates intent.");
        createQuoteCapture(token, ids.bookId(), 113, "A strong input buffer protects player intention.");

        Long sourceReferenceId = createSourceBackedCapture(token, ids.bookId(), 42);
        createDesignLens(token, ids.bookId(), sourceReferenceId);

        String firstToday = mockMvc.perform(get("/api/daily/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentence.text").isNotEmpty())
                .andExpect(jsonPath("$.data.sentence.sourceBacked").value(true))
                .andExpect(jsonPath("$.data.sentence.sourceReference.pageStart").isNumber())
                .andExpect(jsonPath("$.data.prompt.templatePrompt").value(false))
                .andExpect(jsonPath("$.data.prompt.sourceReference.pageStart").value(42))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode first = objectMapper.readTree(firstToday).path("data");
        Long firstSentenceId = first.path("sentence").path("id").asLong();
        Long firstPromptId = first.path("prompt").path("id").asLong();
        String promptSourceType = first.path("prompt").path("sourceType").asText();
        Long promptSourceId = first.path("prompt").path("sourceId").asLong();

        mockMvc.perform(get("/api/daily/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentence.id").value(firstSentenceId))
                .andExpect(jsonPath("$.data.prompt.id").value(firstPromptId));

        mockMvc.perform(post("/api/daily/regenerate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target": "SENTENCE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentence.id").value(not(firstSentenceId)));

        mockMvc.perform(post("/api/daily/reflections")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "target": "PROMPT",
                                  "dailyDesignPromptId": %d,
                                  "reflectionText": "This lens should become a small control-feel prototype."
                                }
                                """.formatted(firstPromptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reflectionText").value("This lens should become a small control-feel prototype."));

        mockMvc.perform(get("/api/mastery/target")
                        .header("Authorization", "Bearer " + token)
                        .queryParam("targetType", promptSourceType)
                        .queryParam("targetId", String.valueOf(promptSourceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familiarityScore").value(1))
                .andExpect(jsonPath("$.data.usefulnessScore").value(1))
                .andExpect(jsonPath("$.data.sourceReference.id").value(sourceReferenceId));

        mockMvc.perform(post("/api/daily/create-prototype-task")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyDesignPromptId": %d,
                                  "title": "Prototype buffered jump feedback"
                                }
                                """.formatted(firstPromptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("PROTOTYPE_TASK"))
                .andExpect(jsonPath("$.data.sourceReference.pageStart").value(42));
    }

    @Test
    void userWithNoSourcesGetsTemplatePromptWithoutFakeSentenceOrPageNumber() throws Exception {
        String token = register("daily-empty-%s@bookos.local".formatted(UUID.randomUUID()), "Daily Empty");

        mockMvc.perform(get("/api/daily/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prompt.templatePrompt").value(true))
                .andExpect(jsonPath("$.data.prompt.sourceType").value("TEMPLATE"))
                .andExpect(jsonPath("$.data.prompt.sourceReference").isEmpty());
    }

    private void createQuoteCapture(String token, Long bookId, int page, String text) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCAC p.%d %s #quote [[Daily Source Test]]"
                                }
                                """.formatted(bookId, page, text)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long captureId = objectMapper.readTree(captureResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private Long createSourceBackedCapture(String token, Long bookId, int page) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCA1 p.%d Use feedback timing as a design lens. #lens [[Feedback Timing]]"
                                }
                                """.formatted(bookId, page)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(captureResponse)
                .path("data")
                .path("sourceReferences")
                .get(0)
                .path("id")
                .asLong();
    }

    private void createDesignLens(String token, Long bookId, Long sourceReferenceId) throws Exception {
        mockMvc.perform(post("/api/knowledge-objects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DESIGN_LENS",
                                  "title": "Feedback Timing Daily Lens %s",
                                  "description": "An original design lens for inspecting when feedback arrives.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "tags": ["daily", "lens"]
                                }
                                """.formatted(UUID.randomUUID(), bookId, sourceReferenceId)))
                .andExpect(status().isCreated());
    }

    private BookLibraryIds createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Daily Resurfacing Book %s",
                                  "subtitle": "Daily source test",
                                  "description": "Testing source-backed daily resurfacing.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/daily.jpg",
                                  "category": "Daily Systems",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["daily", "resurfacing"]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(createBookResponse).path("data").path("id").asLong();

        String libraryResponse = mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userBookId = objectMapper.readTree(libraryResponse).path("data").path("id").asLong();
        return new BookLibraryIds(bookId, userBookId);
    }

    private String loginAsDesigner() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "designer@bookos.local",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
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

        return objectMapper.readTree(response).path("data").path("token").asText();
    }

    private record BookLibraryIds(Long bookId, Long userBookId) {}
}
