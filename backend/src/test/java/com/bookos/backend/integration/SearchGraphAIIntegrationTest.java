package com.bookos.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    void workspaceGraphUsesRealQuoteNodesAndRelationshipFilters() throws Exception {
        String token = register("workspace-graph-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Workspace Graph Owner");
        Long bookId = createBookAndAddToLibrary(token, "Workspace Graph Book " + UUID.randomUUID());
        JsonNode capture = createCapture(token, bookId, "\\uD83D\\uDCAC p.17 Real graph quote about readable source links.");
        Long quoteId = convertCaptureToQuote(token, capture.path("id").asLong());

        JsonNode graph = objectMapper.readTree(mockMvc.perform(get("/api/graph")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId))
                        .param("relationshipType", "SOURCE_OF"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");

        assertThat(StreamSupport.stream(graph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "QUOTE".equals(node.path("type").asText())
                                && quoteId.equals(node.path("entityId").asLong())))
                .isTrue();
        assertThat(StreamSupport.stream(graph.path("edges").spliterator(), false)
                        .allMatch(edge -> "SOURCE_OF".equals(edge.path("type").asText())))
                .isTrue();
        assertThat(graph.toString()).doesNotContain("MDA Framework", "Game Feel", "Fake");
    }

    @Test
    void backlinksResolveLabelsAndRemainOwnerScoped() throws Exception {
        String ownerToken = register("backlink-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Backlink Owner");
        String intruderToken = register("backlink-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Backlink Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Backlink Book " + UUID.randomUUID());
        JsonNode capture = createCapture(ownerToken, bookId, "p.33 Backlink source reference should stay private.");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        mockMvc.perform(post("/api/entity-links")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "BOOK",
                                  "sourceId": %d,
                                  "targetType": "SOURCE_REFERENCE",
                                  "targetId": %d,
                                  "relationType": "SOURCE_OF",
                                  "sourceReferenceId": %d
                                }
                                """.formatted(bookId, sourceReferenceId, sourceReferenceId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/backlinks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("entityType", "BOOK")
                        .param("entityId", String.valueOf(bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].entityType").value("SOURCE_REFERENCE"))
                .andExpect(jsonPath("$.data[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data[0].sourceReference.id").value(sourceReferenceId));

        mockMvc.perform(get("/api/backlinks")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("entityType", "BOOK")
                        .param("entityId", String.valueOf(bookId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void graphFiltersAndManualEntityLinksAreOwnerScopedAndEditable() throws Exception {
        String ownerToken = register("graph-link-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Graph Link Owner");
        String intruderToken = register("graph-link-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Graph Link Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Manual Graph Link Book " + UUID.randomUUID());
        JsonNode capture = createCapture(ownerToken, bookId, "\\uD83D\\uDCA1 p.21 Manual graph curation source. [[Manual Link Concept]]");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        String conceptResponse = mockMvc.perform(post("/api/concepts")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Manual Link Concept",
                                  "description": "Original relationship curation test concept.",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "visibility": "PRIVATE",
                                  "tags": ["graph"]
                                }
                                """.formatted(bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long conceptId = objectMapper.readTree(conceptResponse).path("data").path("id").asLong();

        String linkResponse = mockMvc.perform(post("/api/entity-links")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "BOOK",
                                  "sourceId": %d,
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "relationType": "RELATED_TO",
                                  "sourceReferenceId": %d,
                                  "note": "Manual curation note"
                                }
                                """.formatted(bookId, conceptId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy").value("USER"))
                .andExpect(jsonPath("$.data.systemCreated").value(false))
                .andExpect(jsonPath("$.data.note").value("Manual curation note"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long linkId = objectMapper.readTree(linkResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/entity-links/{id}", linkId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "BOOK",
                                  "sourceId": %d,
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "relationType": "APPLIES_TO",
                                  "sourceReferenceId": %d,
                                  "note": "Updated manual curation note"
                                }
                                """.formatted(bookId, conceptId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relationType").value("APPLIES_TO"))
                .andExpect(jsonPath("$.data.note").value("Updated manual curation note"));

        JsonNode graph = objectMapper.readTree(mockMvc.perform(get("/api/graph")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("bookId", String.valueOf(bookId))
                        .param("entityType", "CONCEPT")
                        .param("relationshipType", "APPLIES_TO")
                        .param("sourceConfidence", "HIGH")
                        .param("depth", "2")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");

        assertThat(StreamSupport.stream(graph.path("edges").spliterator(), false)
                        .anyMatch(edge -> edge.path("entityLinkId").asLong() == linkId
                                && "USER".equals(edge.path("createdBy").asText())
                                && !edge.path("systemCreated").asBoolean()))
                .isTrue();
        assertThat(StreamSupport.stream(graph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "CONCEPT".equals(node.path("type").asText())
                                && node.path("entityId").asLong() == conceptId))
                .isTrue();

        mockMvc.perform(get("/api/graph/concept/{conceptId}", conceptId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[?(@.type == 'CONCEPT')]").isNotEmpty())
                .andExpect(jsonPath("$.data.nodes[?(@.type == 'SOURCE_REFERENCE')]").isNotEmpty());

        mockMvc.perform(get("/api/graph/concept/{conceptId}", conceptId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/entity-links")
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "BOOK",
                                  "sourceId": %d,
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "relationType": "RELATED_TO"
                                }
                                """.formatted(bookId, conceptId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/entity-links/{id}", linkId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        JsonNode systemLinks = objectMapper.readTree(mockMvc.perform(get("/api/entity-links")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("sourceType", "SOURCE_REFERENCE")
                        .param("sourceId", String.valueOf(sourceReferenceId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        Long systemLinkId = StreamSupport.stream(systemLinks.spliterator(), false)
                .filter(node -> "SYSTEM".equals(node.path("createdBy").asText()))
                .map(node -> node.path("id").asLong())
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/entity-links/{id}", systemLinkId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void projectSourceTraceabilityBacklinksAndGraphRemainOwnerScoped() throws Exception {
        String ownerToken = register("project-trace-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Project Trace Owner");
        String intruderToken = register("project-trace-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Project Trace Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Project Trace Book " + UUID.randomUUID());
        JsonNode capture = createCapture(ownerToken, bookId, "\\uD83D\\uDCAC p.64 Source traceability for project work.");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();
        Long quoteId = convertCaptureToQuote(ownerToken, capture.path("id").asLong());
        Long projectId = createProject(ownerToken, "Source Traceability Project " + UUID.randomUUID());

        Long problemId = createProjectProblem(ownerToken, projectId, sourceReferenceId);
        Long applicationId = createProjectApplication(ownerToken, projectId, quoteId, sourceReferenceId);
        Long decisionId = createDesignDecision(ownerToken, projectId, sourceReferenceId);
        Long findingId = createPlaytestFinding(ownerToken, projectId, sourceReferenceId);
        Long lensReviewId = createProjectLensReview(ownerToken, projectId, sourceReferenceId);

        assertProjectSourceLookup(ownerToken, "PROJECT_PROBLEM", problemId, sourceReferenceId);
        assertProjectSourceLookup(ownerToken, "PROJECT_APPLICATION", applicationId, sourceReferenceId);
        assertProjectSourceLookup(ownerToken, "DESIGN_DECISION", decisionId, sourceReferenceId);
        assertProjectSourceLookup(ownerToken, "PLAYTEST_FINDING", findingId, sourceReferenceId);
        assertProjectSourceLookup(ownerToken, "PROJECT_LENS_REVIEW", lensReviewId, sourceReferenceId);

        mockMvc.perform(get("/api/source-references")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("entityType", "PROJECT_APPLICATION")
                        .param("entityId", String.valueOf(applicationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        String linkResponse = mockMvc.perform(post("/api/entity-links")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "PROJECT_APPLICATION",
                                  "sourceId": %d,
                                  "targetType": "DESIGN_DECISION",
                                  "targetId": %d,
                                  "relationType": "EVIDENCE_FOR",
                                  "sourceReferenceId": %d,
                                  "note": "Project application informs this decision."
                                }
                                """.formatted(applicationId, decisionId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long entityLinkId = objectMapper.readTree(linkResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/backlinks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("entityType", "PROJECT_APPLICATION")
                        .param("entityId", String.valueOf(applicationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].entityType").value("DESIGN_DECISION"))
                .andExpect(jsonPath("$.data[0].sourceReference.id").value(sourceReferenceId))
                .andExpect(jsonPath("$.data[0].sourceReference.pageStart").value(64));

        mockMvc.perform(get("/api/backlinks")
                        .header("Authorization", "Bearer " + intruderToken)
                        .param("entityType", "PROJECT_APPLICATION")
                        .param("entityId", String.valueOf(applicationId)))
                .andExpect(status().isNotFound());

        JsonNode projectGraph = objectMapper.readTree(mockMvc.perform(get("/api/graph/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(StreamSupport.stream(projectGraph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "PROJECT_APPLICATION".equals(node.path("type").asText())
                                && node.path("entityId").asLong() == applicationId))
                .isTrue();
        assertThat(StreamSupport.stream(projectGraph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "SOURCE_REFERENCE".equals(node.path("type").asText())
                                && node.path("entityId").asLong() == sourceReferenceId))
                .isTrue();
        assertThat(StreamSupport.stream(projectGraph.path("edges").spliterator(), false)
                        .anyMatch(edge -> edge.path("entityLinkId").asLong() == entityLinkId
                                && "USER".equals(edge.path("createdBy").asText())
                                && "EVIDENCE_FOR".equals(edge.path("type").asText())))
                .isTrue();
        assertThat(projectGraph.toString()).doesNotContain("Fake", "MDA Framework", "Game Feel");

        mockMvc.perform(get("/api/graph/project/{projectId}", projectId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/entity-links/{id}", entityLinkId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/entity-links/{id}", entityLinkId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
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
                                  "draftJson": "{\\"provider\\":\\"MockAIProvider\\",\\"type\\":\\"NOTE_SUMMARY\\",\\"sourceTitle\\":\\"Mock AI Book\\",\\"summary\\":\\"Edited draft text\\"}"
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

    @Test
    void allAISuggestionEndpointsCreateValidatedDraftsWithSourceReferences() throws Exception {
        String token = register("ai-endpoints-%s@bookos.local".formatted(UUID.randomUUID()), "AI Endpoint Owner");
        Long bookId = createBookAndAddToLibrary(token, "AI Endpoint Book " + UUID.randomUUID());
        JsonNode capture = createCapture(token, bookId, "\uD83D\uDCA1 p.28 AI endpoint source context for draft-only suggestions.");
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        assertAISuggestionDraft(token, "/api/ai/suggestions/note-summary", "NOTE_SUMMARY", bookId, sourceReferenceId);
        assertAISuggestionDraft(token, "/api/ai/suggestions/extract-actions", "EXTRACT_ACTIONS", bookId, sourceReferenceId);
        assertAISuggestionDraft(token, "/api/ai/suggestions/extract-concepts", "EXTRACT_CONCEPTS", bookId, sourceReferenceId);
        assertAISuggestionDraft(token, "/api/ai/suggestions/design-lenses", "SUGGEST_DESIGN_LENSES", bookId, sourceReferenceId);
        assertAISuggestionDraft(token, "/api/ai/suggestions/project-applications", "SUGGEST_PROJECT_APPLICATIONS", bookId, sourceReferenceId);
        assertAISuggestionDraft(token, "/api/ai/suggestions/forum-thread", "FORUM_THREAD_DRAFT", bookId, sourceReferenceId);

        JsonNode suggestions = objectMapper.readTree(mockMvc.perform(get("/api/ai/suggestions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");

        assertThat(StreamSupport.stream(suggestions.spliterator(), false)
                        .filter(node -> node.path("sourceReferenceId").asLong() == sourceReferenceId)
                        .count())
                .isGreaterThanOrEqualTo(6);
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

    private Long convertCaptureToQuote(String token, Long captureId) throws Exception {
        String response = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("targetId").asLong();
    }

    private void assertAISuggestionDraft(
            String token,
            String path,
            String expectedType,
            Long bookId,
            Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post(path)
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
                .andExpect(jsonPath("$.data.providerName").value("MockAIProvider"))
                .andExpect(jsonPath("$.data.suggestionType").value(expectedType))
                .andExpect(jsonPath("$.data.sourceReferenceId").value(sourceReferenceId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode draftJson = objectMapper.readTree(objectMapper.readTree(response).path("data").path("draftJson").asText());
        assertThat(draftJson.path("provider").asText()).isEqualTo("MockAIProvider");
        assertThat(draftJson.path("type").asText()).isEqualTo(expectedType);
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

    private Long createProject(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Graph/source traceability test project.",
                                  "genre": "Puzzle",
                                  "platform": "Web",
                                  "stage": "IDEATION",
                                  "visibility": "PRIVATE",
                                  "progressPercent": 10
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createProjectProblem(String token, Long projectId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/problems", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Traceability problem",
                                  "description": "Problem linked to a source reference.",
                                  "status": "OPEN",
                                  "priority": "HIGH",
                                  "relatedSourceReferenceId": %d
                                }
                                """.formatted(sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createProjectApplication(String token, Long projectId, Long quoteId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/applications", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceEntityType": "QUOTE",
                                  "sourceEntityId": %d,
                                  "sourceReferenceId": %d,
                                  "applicationType": "TRACE_TEST",
                                  "title": "Apply traceability quote",
                                  "description": "Use this source-backed quote in a project application.",
                                  "status": "OPEN"
                                }
                                """.formatted(quoteId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createDesignDecision(String token, Long projectId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/decisions", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Traceability decision",
                                  "decision": "Keep the source-backed choice.",
                                  "rationale": "The related source explains why this decision exists.",
                                  "tradeoffs": "The choice needs validation.",
                                  "sourceReferenceId": %d,
                                  "status": "OPEN"
                                }
                                """.formatted(sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createPlaytestFinding(String token, Long projectId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/playtest-findings", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Traceability finding",
                                  "observation": "Players reacted to the source-backed mechanic.",
                                  "severity": "MEDIUM",
                                  "recommendation": "Run another source-backed test.",
                                  "sourceReferenceId": %d,
                                  "status": "OPEN"
                                }
                                """.formatted(sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private Long createProjectLensReview(String token, Long projectId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/lens-reviews", projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "Does the project preserve source traceability?",
                                  "answer": "Yes, this review points back to a source reference.",
                                  "score": 7,
                                  "status": "REVIEWED",
                                  "sourceReferenceId": %d
                                }
                                """.formatted(sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private void assertProjectSourceLookup(String token, String entityType, Long entityId, Long sourceReferenceId) throws Exception {
        mockMvc.perform(get("/api/source-references")
                        .header("Authorization", "Bearer " + token)
                        .param("entityType", entityType)
                        .param("entityId", String.valueOf(entityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(sourceReferenceId))
                .andExpect(jsonPath("$.data[0].pageStart").value(64))
                .andExpect(jsonPath("$.data[0].sourceConfidence").value("HIGH"));
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
