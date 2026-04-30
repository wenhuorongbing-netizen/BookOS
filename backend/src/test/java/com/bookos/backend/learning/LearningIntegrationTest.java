package com.bookos.backend.learning;

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
class LearningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void readingReviewMasteryAndAnalyticsAreUserScopedAndSourceBacked() throws Exception {
        String ownerToken = register("learn-owner-%s@example.test".formatted(UUID.randomUUID()), "Learning Owner");
        String intruderToken = register("learn-intruder-%s@example.test".formatted(UUID.randomUUID()), "Learning Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken);
        QuoteFixture quote = createSourceBackedQuote(ownerToken, bookId);

        String readingResponse = mockMvc.perform(post("/api/reading-sessions/start")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "startPage": 10,
                                  "reflection": "Starting a focused reading pass."
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long readingSessionId = data(readingResponse).path("id").asLong();

        mockMvc.perform(put("/api/reading-sessions/{id}/finish", readingSessionId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endPage": 22,
                                  "minutesRead": 18,
                                  "notesCount": 1,
                                  "capturesCount": 1,
                                  "reflection": "The quote became review material."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endPage").value(22))
                .andExpect(jsonPath("$.data.minutesRead").value(18));

        String reviewResponse = mockMvc.perform(post("/api/review/generate-from-book")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "Book source review",
                                  "mode": "SOURCE_REVIEW",
                                  "limit": 4
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.scopeType").value("BOOK"))
                .andExpect(jsonPath("$.data.items[0].sourceReference.id").value(quote.sourceReferenceId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode reviewData = data(reviewResponse);
        Long reviewSessionId = reviewData.path("id").asLong();
        Long reviewItemId = reviewData.path("items").get(0).path("id").asLong();

        mockMvc.perform(get("/api/review/sessions/{id}", reviewSessionId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/review/items/{id}", reviewItemId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userResponse": "This quote affects my control feedback loop.",
                                  "status": "COMPLETED",
                                  "confidenceScore": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.confidenceScore").value(4));

        mockMvc.perform(get("/api/mastery/target")
                        .header("Authorization", bearer(ownerToken))
                        .queryParam("targetType", "QUOTE")
                        .queryParam("targetId", String.valueOf(quote.quoteId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familiarityScore").value(4))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        mockMvc.perform(get("/api/mastery/target")
                        .header("Authorization", bearer(intruderToken))
                        .queryParam("targetType", "QUOTE")
                        .queryParam("targetId", String.valueOf(quote.quoteId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/analytics/reading")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(1))
                .andExpect(jsonPath("$.data.quotesCount").value(1))
                .andExpect(jsonPath("$.data.totalMinutesRead").value(18));

        mockMvc.perform(get("/api/analytics/knowledge")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.masteryTargets").value(1))
                .andExpect(jsonPath("$.data.completedReviewItems").value(1));

        mockMvc.perform(get("/api/analytics/reading")
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(0))
                .andExpect(jsonPath("$.data.quotesCount").value(0))
                .andExpect(jsonPath("$.data.totalMinutesRead").value(0));
    }

    @Test
    void emptyAnalyticsResponsesAreHonestZeros() throws Exception {
        String token = register("empty-learning-%s@example.test".formatted(UUID.randomUUID()), "Empty Learning");

        mockMvc.perform(get("/api/analytics/reading")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(0))
                .andExpect(jsonPath("$.data.currentlyReadingBooks").value(0))
                .andExpect(jsonPath("$.data.notesCount").value(0))
                .andExpect(jsonPath("$.data.capturesCount").value(0))
                .andExpect(jsonPath("$.data.quotesCount").value(0))
                .andExpect(jsonPath("$.data.projectApplicationsCount").value(0))
                .andExpect(jsonPath("$.data.reviewSessionsCount").value(0))
                .andExpect(jsonPath("$.data.totalMinutesRead").value(0))
                .andExpect(jsonPath("$.data.mostActiveBooks").isEmpty());

        mockMvc.perform(get("/api/analytics/knowledge")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conceptsCount").value(0))
                .andExpect(jsonPath("$.data.knowledgeObjectsCount").value(0))
                .andExpect(jsonPath("$.data.masteryTargets").value(0))
                .andExpect(jsonPath("$.data.reviewItems").value(0))
                .andExpect(jsonPath("$.data.completedReviewItems").value(0))
                .andExpect(jsonPath("$.data.mostLinkedConcepts").isEmpty());

        mockMvc.perform(get("/api/review/sessions")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mockMvc.perform(get("/api/mastery")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void conceptProjectReviewGenerationAndManualItemsPreserveOwnedSources() throws Exception {
        String ownerToken = register("learn-project-owner-%s@example.test".formatted(UUID.randomUUID()), "Learning Project Owner");
        String intruderToken = register("learn-project-intruder-%s@example.test".formatted(UUID.randomUUID()), "Learning Project Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken);
        QuoteFixture quote = createSourceBackedQuote(ownerToken, bookId);
        Long conceptId = createConcept(ownerToken, bookId, quote.sourceReferenceId());

        String conceptReviewResponse = mockMvc.perform(post("/api/review/generate-from-concept")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "Concept source review",
                                  "mode": "SOURCE_REVIEW",
                                  "limit": 4
                                }
                                """.formatted(conceptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.scopeType").value("CONCEPT"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("CONCEPT"))
                .andExpect(jsonPath("$.data.items[0].sourceReference.id").value(quote.sourceReferenceId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long conceptReviewItemId = data(conceptReviewResponse).path("items").get(0).path("id").asLong();

        mockMvc.perform(put("/api/review/items/{id}", conceptReviewItemId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userResponse": "This concept now has a real review answer.",
                                  "status": "COMPLETED",
                                  "confidenceScore": 5
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/mastery/target")
                        .header("Authorization", bearer(ownerToken))
                        .queryParam("targetType", "CONCEPT")
                        .queryParam("targetId", String.valueOf(conceptId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familiarityScore").value(5))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        String manualSessionResponse = mockMvc.perform(post("/api/review/sessions")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Manual source item review",
                                  "mode": "SOURCE_REVIEW",
                                  "scopeType": "GENERAL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long manualSessionId = data(manualSessionResponse).path("id").asLong();

        mockMvc.perform(post("/api/review/sessions/{id}/items", manualSessionId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "QUOTE",
                                  "targetId": %d,
                                  "sourceReferenceId": %d,
                                  "prompt": "Explain how the quote changes a design decision."
                                }
                                """.formatted(quote.quoteId(), quote.sourceReferenceId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        mockMvc.perform(post("/api/review/sessions/{id}/items", manualSessionId)
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "QUOTE",
                                  "targetId": %d,
                                  "sourceReferenceId": %d,
                                  "prompt": "This should not cross users."
                                }
                                """.formatted(quote.quoteId(), quote.sourceReferenceId())))
                .andExpect(status().isNotFound());

        Long projectId = createProject(ownerToken);
        createProjectApplication(ownerToken, projectId, quote.sourceReferenceId());

        mockMvc.perform(post("/api/review/generate-from-project")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "Project application review",
                                  "mode": "SOURCE_REVIEW",
                                  "limit": 4
                                }
                                """.formatted(projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.scopeType").value("PROJECT"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("PROJECT_APPLICATION"))
                .andExpect(jsonPath("$.data.items[0].sourceReference.id").value(quote.sourceReferenceId()));

        mockMvc.perform(post("/api/review/generate-from-project")
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "Cross-user project review"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/mastery/target")
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "familiarityScore": 3,
                                  "usefulnessScore": 3,
                                  "sourceReferenceId": %d
                                }
                                """.formatted(conceptId, quote.sourceReferenceId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/analytics/books/{bookId}", bookId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learning Analytics Test Book %s",
                                  "subtitle": "Review MVP",
                                  "description": "Original test metadata only.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": null,
                                  "category": "Learning",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["learning", "review"]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long bookId = data(response).path("id").asLong();
        mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
        return bookId;
    }

    private QuoteFixture createSourceBackedQuote(String token, Long bookId) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCAC p.12 Good feedback makes player intent visible. #quote [[Feedback]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long captureId = data(captureResponse).path("id").asLong();

        String conversionResponse = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long quoteId = data(conversionResponse).path("targetId").asLong();

        String quoteResponse = mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long sourceReferenceId = data(quoteResponse).path("sourceReference").path("id").asLong();
        return new QuoteFixture(quoteId, sourceReferenceId);
    }

    private Long createConcept(String token, Long bookId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/concepts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Learning Concept %s",
                                  "description": "Original concept used for learning review tests.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "ontologyLayer": "Player Experience",
                                  "tags": ["learning", "review"]
                                }
                                """.formatted(UUID.randomUUID(), bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstSourceReference.id").value(sourceReferenceId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
    }

    private Long createProject(String token) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learning Project %s",
                                  "description": "Project used for source-backed review generation.",
                                  "genre": "Puzzle",
                                  "platform": "PC",
                                  "stage": "PROTOTYPE",
                                  "visibility": "PRIVATE",
                                  "progressPercent": 20
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
    }

    private void createProjectApplication(String token, Long projectId, Long sourceReferenceId) throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/applications", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceEntityType": "SOURCE_REFERENCE",
                                  "sourceEntityId": %d,
                                  "sourceReferenceId": %d,
                                  "applicationType": "DESIGN_ACTION",
                                  "title": "Apply feedback timing to prototype loop",
                                  "description": "Use the source-backed quote as a design action.",
                                  "status": "ACTIVE"
                                }
                                """.formatted(sourceReferenceId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReference.id").value(sourceReferenceId));
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
        return data(response).path("token").asText();
    }

    private JsonNode data(String response) throws Exception {
        return objectMapper.readTree(response).path("data");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record QuoteFixture(Long quoteId, Long sourceReferenceId) {}
}
