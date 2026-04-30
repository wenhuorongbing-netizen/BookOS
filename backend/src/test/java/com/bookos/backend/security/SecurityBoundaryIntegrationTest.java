package com.bookos.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.stream.StreamSupport;
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
class SecurityBoundaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestsUseConsistentJsonError() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void crossUserPrivateReadingObjectsAreDenied() throws Exception {
        String unique = "security-boundary-" + UUID.randomUUID();
        String ownerToken = register("owner-%s@bookos.local".formatted(UUID.randomUUID()), "Boundary Owner");
        String intruderToken = register("intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Boundary Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Private Boundary Book " + unique);

        JsonNode note = createNote(ownerToken, bookId, unique);
        Long noteId = note.path("id").asLong();

        JsonNode capture = createCapture(ownerToken, bookId, "\uD83D\uDCA1 p.12 " + unique + " private capture [[Security Boundary]]");
        Long captureId = capture.path("id").asLong();
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        JsonNode quoteCapture = createCapture(ownerToken, bookId, "\uD83D\uDCAC p.22 " + unique + " private quote [[Security Boundary]]");
        Long quoteId = convertCaptureToQuote(ownerToken, quoteCapture.path("id").asLong());

        JsonNode actionCapture = createCapture(ownerToken, bookId, "\u2705 p.32 " + unique + " private action [[Security Boundary]]");
        Long actionItemId = convertCaptureToActionItem(ownerToken, actionCapture.path("id").asLong());

        mockMvc.perform(get("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/source-references/{id}", sourceReferenceId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/captures/{id}/convert", captureId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"NOTE\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/action-items/{id}/complete", actionItemId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/graph/book/{bookId}", bookId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        JsonNode intruderSearchResults = objectMapper.readTree(mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("q", unique))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(intruderSearchResults).isEmpty();
    }

    @Test
    void crossUserKnowledgeProjectImportExportAndLearningBoundariesAreDenied() throws Exception {
        String unique = "extended-boundary-" + UUID.randomUUID();
        String ownerToken = register("extended-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Extended Boundary Owner");
        String intruderToken = register("extended-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Extended Boundary Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Extended Private Book " + unique);
        JsonNode capture = createCapture(ownerToken, bookId, "\\uD83D\\uDCA1 p.40 " + unique + " private concept [[Extended Boundary Concept]]");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();
        Long conceptId = createConcept(ownerToken, bookId, sourceReferenceId, unique);
        Long projectId = createProject(ownerToken, unique);
        Long reviewSessionId = createBookReview(ownerToken, bookId);

        mockMvc.perform(get("/api/concepts/{id}", conceptId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/projects/{id}", projectId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/graph/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/export/book/{bookId}/json", bookId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        JsonNode intruderExport = objectMapper.readTree(mockMvc.perform(get("/api/export/json")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(intruderExport.toString()).doesNotContain(unique);

        mockMvc.perform(get("/api/review/sessions/{id}", reviewSessionId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/mastery/target")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "familiarityScore": 4,
                                  "usefulnessScore": 5,
                                  "sourceReferenceId": %d
                                }
                                """.formatted(conceptId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceReference.id").value(sourceReferenceId));

        mockMvc.perform(get("/api/mastery/target")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("targetType", "CONCEPT")
                        .param("targetId", String.valueOf(conceptId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/analytics/reading")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(0))
                .andExpect(jsonPath("$.data.capturesCount").value(0));

        mockMvc.perform(post("/api/projects/{projectId}/apply/concept", projectId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceId": %d,
                                  "title": "Invalid cross-user concept application"
                                }
                                """.formatted(conceptId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void forumRelatedEntityAndSourceContextAreScoped() throws Exception {
        String unique = "forum-boundary-" + UUID.randomUUID();
        String ownerToken = register("forum-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Forum Boundary Owner");
        String intruderToken = register("forum-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Forum Boundary Intruder");
        Long ownerCategoryId = firstCategoryId(ownerToken);
        Long intruderCategoryId = firstCategoryId(intruderToken);
        Long bookId = createBookAndAddToLibrary(ownerToken, "Forum Private Book " + unique);
        JsonNode capture = createCapture(ownerToken, bookId, "\uD83D\uDCAC p.44 " + unique + " forum quote [[Forum Boundary]]");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();
        Long quoteId = convertCaptureToQuote(ownerToken, capture.path("id").asLong());

        mockMvc.perform(post("/api/forum/threads")
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Invalid private quote attachment",
                                  "bodyMarkdown": "Should not attach another user's quote.",
                                  "relatedEntityType": "QUOTE",
                                  "relatedEntityId": %d,
                                  "visibility": "SHARED"
                                }
                                """.formatted(intruderCategoryId, quoteId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/forum/threads")
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Invalid private source attachment",
                                  "bodyMarkdown": "Should not attach another user's source.",
                                  "sourceReferenceId": %d,
                                  "visibility": "SHARED"
                                }
                                """.formatted(intruderCategoryId, sourceReferenceId)))
                .andExpect(status().isNotFound());

        Long sharedThreadId = createForumThread(
                ownerToken,
                ownerCategoryId,
                "Shared thread with hidden private source " + unique,
                "Shared discussion body " + unique,
                "QUOTE",
                quoteId,
                bookId,
                sourceReferenceId);

        JsonNode intruderThread = objectMapper.readTree(mockMvc.perform(get("/api/forum/threads/{id}", sharedThreadId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(intruderThread.path("relatedEntityType").isNull()).isTrue();
        assertThat(intruderThread.path("relatedEntityId").isNull()).isTrue();
        assertThat(intruderThread.path("relatedBookId").isNull()).isTrue();
        assertThat(intruderThread.path("sourceReferenceId").isNull()).isTrue();
        assertThat(intruderThread.path("sourceReference").isNull()).isTrue();

        JsonNode searchResults = objectMapper.readTree(mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("q", unique)
                        .param("type", "FORUM_THREAD"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        JsonNode visibleThreadResult = StreamSupport.stream(searchResults.spliterator(), false)
                .filter(node -> node.path("id").asLong() == sharedThreadId)
                .findFirst()
                .orElseThrow();
        assertThat(visibleThreadResult.path("bookId").isNull()).isTrue();
        assertThat(visibleThreadResult.path("sourceReferenceId").isNull()).isTrue();

        Long firstCommentId = createForumComment(ownerToken, sharedThreadId, "Parent comment");
        Long secondThreadId = createForumThread(
                ownerToken,
                ownerCategoryId,
                "Second thread " + unique,
                "Second thread body",
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/forum/threads/{id}/comments", secondThreadId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bodyMarkdown": "Wrong parent thread.",
                                  "parentCommentId": %d
                                }
                                """.formatted(firstCommentId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aiSuggestionsAreOwnerScopedAndDraftOnly() throws Exception {
        String unique = "ai-boundary-" + UUID.randomUUID();
        String ownerToken = register("ai-boundary-owner-%s@bookos.local".formatted(UUID.randomUUID()), "AI Boundary Owner");
        String intruderToken = register("ai-boundary-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "AI Boundary Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "AI Boundary Book " + unique);
        String rawText = "\uD83D\uDCA1 p.54 " + unique + " original source text remains unchanged.";
        JsonNode capture = createCapture(ownerToken, bookId, rawText);
        Long captureId = capture.path("id").asLong();
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        Long suggestionId = createAISuggestion(ownerToken, bookId, sourceReferenceId);

        JsonNode intruderSuggestions = objectMapper.readTree(mockMvc.perform(get("/api/ai/suggestions")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(StreamSupport.stream(intruderSuggestions.spliterator(), false)
                        .anyMatch(node -> node.path("id").asLong() == suggestionId))
                .isFalse();

        mockMvc.perform(put("/api/ai/suggestions/{id}/accept", suggestionId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/ai/suggestions/{id}/edit", suggestionId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "draftText": "Intruder edit",
                                  "draftJson": "{\\"summary\\":\\"Intruder edit\\"}"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/ai/suggestions/{id}/accept", suggestionId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        mockMvc.perform(put("/api/ai/suggestions/{id}/edit", suggestionId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "draftText": "Late edit",
                                  "draftJson": "{\\"summary\\":\\"Late edit\\"}"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/captures/{id}", captureId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawText").value(rawText));
    }

    private JsonNode createNote(String token, Long bookId, String unique) throws Exception {
        String response = mockMvc.perform(post("/api/books/{bookId}/notes", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Private note %s",
                                  "markdown": "\\uD83D\\uDCA1 p.18 %s private note [[Security Boundary]]",
                                  "visibility": "PRIVATE"
                                }
                                """.formatted(unique, unique)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private JsonNode createCapture(String token, Long bookId, String rawText) throws Exception {
        String response = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "%s"
                                }
                                """.formatted(bookId, rawText)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data");
    }

    private Long convertCaptureToQuote(String token, Long captureId) throws Exception {
        String response = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("targetId").asLong();
    }

    private Long convertCaptureToActionItem(String token, Long captureId) throws Exception {
        String response = mockMvc.perform(post("/api/captures/{id}/convert/action-item", captureId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("targetId").asLong();
    }

    private Long createConcept(String token, Long bookId, Long sourceReferenceId, String unique) throws Exception {
        String response = mockMvc.perform(post("/api/concepts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Extended Boundary Concept %s",
                                  "description": "Private concept for ownership boundary testing.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "ontologyLayer": "Security",
                                  "tags": ["security"]
                                }
                                """.formatted(unique, bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createProject(String token, String unique) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Extended Boundary Project %s",
                                  "description": "Private project for ownership boundary testing.",
                                  "genre": "Puzzle",
                                  "platform": "Web",
                                  "stage": "IDEATION",
                                  "visibility": "PRIVATE",
                                  "progressPercent": 5
                                }
                                """.formatted(unique)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createBookReview(String token, Long bookId) throws Exception {
        String response = mockMvc.perform(post("/api/review/generate-from-book")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "Private book review",
                                  "mode": "SOURCE_REVIEW",
                                  "limit": 4
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createAISuggestion(String token, Long bookId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/ai/suggestions/note-summary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "sourceReferenceId": %d
                                }
                                """.formatted(bookId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createForumThread(
            String token,
            Long categoryId,
            String title,
            String body,
            String relatedEntityType,
            Long relatedEntityId,
            Long relatedBookId,
            Long sourceReferenceId) throws Exception {
        String relatedFields = relatedEntityType == null
                ? ""
                : """
                                  "relatedEntityType": "%s",
                                  "relatedEntityId": %d,
                        """.formatted(relatedEntityType, relatedEntityId);
        String bookField = relatedBookId == null ? "" : "\"relatedBookId\": %d,".formatted(relatedBookId);
        String sourceField = sourceReferenceId == null ? "" : "\"sourceReferenceId\": %d,".formatted(sourceReferenceId);
        String response = mockMvc.perform(post("/api/forum/threads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "%s",
                                  "bodyMarkdown": "%s",
                                  %s
                                  %s
                                  %s
                                  "visibility": "SHARED"
                                }
                                """.formatted(categoryId, title, body, relatedFields, bookField, sourceField)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createForumComment(String token, Long threadId, String body) throws Exception {
        String response = mockMvc.perform(post("/api/forum/threads/{id}/comments", threadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bodyMarkdown": "%s"
                                }
                                """.formatted(body)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long firstCategoryId(String token) throws Exception {
        String response = mockMvc.perform(get("/api/forum/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").get(0).path("id").asLong();
    }

    private Long createBookAndAddToLibrary(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "subtitle": "Security boundary test",
                                  "description": "Private integration test book.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/security.jpg",
                                  "category": "Security",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["security"]
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(response).path("data").path("id").asLong();
        mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
        return bookId;
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
}
