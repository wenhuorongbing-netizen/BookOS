package com.bookos.backend.integration;

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
class SearchGraphAIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchRespectsOwnedContentBoundaries() throws Exception {
        String unique = "search-boundary-" + UUID.randomUUID();
        String ownerToken = register("search-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Search Owner");
        String intruderToken = register("search-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Search Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Private Search Book " + unique);

        createCapture(ownerToken, bookId, "p.12 " + unique + " source text should stay private.");

        JsonNode ownerResults = objectMapper.readTree(mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("q", unique))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(StreamSupport.stream(ownerResults.spliterator(), false)
                        .anyMatch(node -> unique.equals(extractSearchNeedle(node, unique))))
                .isTrue();

        JsonNode intruderResults = objectMapper.readTree(mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("q", unique))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(intruderResults.size()).isZero();
    }

    @Test
    void graphUsesOnlyRealNodesForAnEmptyBook() throws Exception {
        String token = register("graph-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Graph Owner");
        Long bookId = createBookAndAddToLibrary(token, "Empty Graph Book " + UUID.randomUUID());

        JsonNode graph = objectMapper.readTree(mockMvc.perform(get("/api/graph/book/{bookId}", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[0].type").value("BOOK"))
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");

        assertThat(graph.path("nodes").size()).isEqualTo(1);
        assertThat(graph.path("edges").size()).isZero();
        assertThat(graph.toString()).doesNotContain("MDA Framework", "Game Feel", "Fake");
    }

    @Test
    void mockAISuggestionDraftLifecycleDoesNotOverwriteSourceContent() throws Exception {
        String token = register("ai-owner-%s@bookos.local".formatted(UUID.randomUUID()), "AI Owner");
        Long bookId = createBookAndAddToLibrary(token, "Mock AI Book " + UUID.randomUUID());
        String rawText = "p.44 MockAI source text remains unchanged after accept.";
        JsonNode capture = createCapture(token, bookId, rawText);
        Long captureId = capture.path("id").asLong();
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        String createdResponse = mockMvc.perform(post("/api/ai/suggestions/note-summary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "sourceReferenceId": %d
                                }
                                """.formatted(bookId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providerName").value("MockAIProvider"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.sourceReferenceId").value(sourceReferenceId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long suggestionId = objectMapper.readTree(createdResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/ai/suggestions/{id}/edit", suggestionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "draftText": "Edited draft text",
                                  "draftJson": "{\\"summary\\":\\"Edited draft text\\",\\"provider\\":\\"MockAIProvider\\"}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.draftText").value("Edited draft text"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        mockMvc.perform(put("/api/ai/suggestions/{id}/accept", suggestionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        mockMvc.perform(get("/api/captures/{id}", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rawText").value(rawText));

        String secondResponse = mockMvc.perform(post("/api/ai/suggestions/extract-actions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "sourceReferenceId": %d
                                }
                                """.formatted(bookId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long secondSuggestionId = objectMapper.readTree(secondResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/ai/suggestions/{id}/reject", secondSuggestionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    private String extractSearchNeedle(JsonNode node, String unique) {
        String title = node.path("title").asText("");
        String excerpt = node.path("excerpt").asText("");
        return title.contains(unique) || excerpt.contains(unique) ? unique : "";
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

    private Long createBookAndAddToLibrary(String token, String title) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "subtitle": "Integration source",
                                  "description": "Integration test book.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/integration.jpg",
                                  "category": "Integration",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["integration"]
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(createBookResponse).path("data").path("id").asLong();

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
