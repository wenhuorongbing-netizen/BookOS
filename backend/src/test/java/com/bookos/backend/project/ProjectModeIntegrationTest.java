package com.bookos.backend.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class ProjectModeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectCrudIsUserOwnedAndCrossUserAccessIsHidden() throws Exception {
        String ownerToken = register("project-owner-%s@bookos.local".formatted(UUID.randomUUID()), "Project Owner");
        String intruderToken = register("project-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Project Intruder");

        Long projectId = createProject(ownerToken, "Source-backed Puzzle Prototype");

        mockMvc.perform(get("/api/projects/{id}", projectId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Source-backed Puzzle Prototype"))
                .andExpect(jsonPath("$.data.visibility").value("PRIVATE"));

        mockMvc.perform(get("/api/projects/{id}", projectId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Source-backed Puzzle Prototype RC",
                                  "description": "Updated project brief.",
                                  "genre": "Puzzle",
                                  "platform": "Web",
                                  "stage": "PROTOTYPE",
                                  "visibility": "PRIVATE",
                                  "progressPercent": 35
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage").value("PROTOTYPE"))
                .andExpect(jsonPath("$.data.progressPercent").value(35));

        mockMvc.perform(delete("/api/projects/{id}", projectId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/{id}", projectId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void projectApplicationPreservesSourceReferenceAndQuoteCanBeApplied() throws Exception {
        String token = register("project-quote-%s@bookos.local".formatted(UUID.randomUUID()), "Project Quote");
        Long projectId = createProject(token, "Quote Application Project");
        Long bookId = createBookAndAddToLibrary(token);
        QuoteFixture quote = createSourceBackedQuote(token, bookId, "Jump timing creates readable intention.");

        mockMvc.perform(post("/api/projects/{projectId}/applications", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceEntityType": "QUOTE",
                                  "sourceEntityId": %d,
                                  "sourceReferenceId": %d,
                                  "applicationType": "MECHANIC_TEST",
                                  "title": "Apply jump timing",
                                  "description": "Use the quote to shape jump anticipation.",
                                  "status": "OPEN"
                                }
                                """.formatted(quote.quoteId(), quote.sourceReferenceId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceEntityType").value("QUOTE"))
                .andExpect(jsonPath("$.data.sourceEntityId").value(quote.quoteId()))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()))
                .andExpect(jsonPath("$.data.sourceReference.pageStart").value(77));

        mockMvc.perform(post("/api/projects/{projectId}/apply/quote", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceId": %d,
                                  "title": "Apply quote through integration endpoint"
                                }
                                """.formatted(quote.quoteId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceEntityType").value("QUOTE"))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()))
                .andExpect(jsonPath("$.data.sourceReference.pageStart").value(77));
    }

    @Test
    void conceptAndKnowledgeObjectCanBeAppliedWithReviewedSourceContext() throws Exception {
        String token = register("project-concept-%s@bookos.local".formatted(UUID.randomUUID()), "Project Concept");
        Long projectId = createProject(token, "Concept Application Project");
        Long bookId = createBookAndAddToLibrary(token);
        QuoteFixture quote = createSourceBackedQuote(token, bookId, "Feedback loops clarify player intent.");
        Long conceptId = createConcept(token, bookId, quote.sourceReferenceId());
        Long knowledgeObjectId = createKnowledgeObject(token, bookId, conceptId, quote.sourceReferenceId());

        mockMvc.perform(post("/api/projects/{projectId}/apply/concept", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceId": %d,
                                  "title": "Apply feedback loop"
                                }
                                """.formatted(conceptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceEntityType").value("CONCEPT"))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        mockMvc.perform(post("/api/projects/{projectId}/apply/knowledge-object", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceId": %d,
                                  "applicationType": "DESIGN_LENS_REVIEW"
                                }
                                """.formatted(knowledgeObjectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceEntityType").value("KNOWLEDGE_OBJECT"))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        mockMvc.perform(post("/api/projects/{projectId}/knowledge-links", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "CONCEPT",
                                  "targetId": %d,
                                  "relationshipType": "APPLIES_TO",
                                  "note": "Use this loop in the tutorial prototype.",
                                  "sourceReferenceId": %d
                                }
                                """.formatted(conceptId, quote.sourceReferenceId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetType").value("CONCEPT"))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));
    }

    @Test
    void projectLensReviewAndPlaytestFindingCrudWorkForOwner() throws Exception {
        String token = register("project-playtest-%s@bookos.local".formatted(UUID.randomUUID()), "Project Playtest");
        Long projectId = createProject(token, "Playtest Project");
        Long bookId = createBookAndAddToLibrary(token);
        QuoteFixture quote = createSourceBackedQuote(token, bookId, "Challenge should reveal useful decisions.");
        Long conceptId = createConcept(token, bookId, quote.sourceReferenceId());
        Long lensId = createKnowledgeObject(token, bookId, conceptId, quote.sourceReferenceId());

        mockMvc.perform(post("/api/projects/{projectId}/lens-reviews", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "knowledgeObjectId": %d,
                                  "question": "Does this mechanic create meaningful choice?",
                                  "answer": "It creates a readable risk window.",
                                  "score": 8,
                                  "status": "REVIEWED",
                                  "sourceReferenceId": %d
                                }
                                """.formatted(lensId, quote.sourceReferenceId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.knowledgeObjectId").value(lensId))
                .andExpect(jsonPath("$.data.score").value(8))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()));

        Long planId = createPlaytestPlan(token, projectId);
        mockMvc.perform(put("/api/playtest-plans/{id}", planId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First loop test",
                                  "hypothesis": "Players understand the jump timing.",
                                  "targetPlayers": "3 designers",
                                  "tasks": "Run the first room twice.",
                                  "successCriteria": "Players state the timing rule.",
                                  "status": "READY"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));

        String findingResponse = mockMvc.perform(post("/api/projects/{projectId}/playtest-findings", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Players miss the timing cue",
                                  "observation": "Two players jumped early.",
                                  "severity": "HIGH",
                                  "recommendation": "Add stronger anticipation animation.",
                                  "sourceReferenceId": %d,
                                  "status": "OPEN"
                                }
                                """.formatted(quote.sourceReferenceId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.severity").value("HIGH"))
                .andExpect(jsonPath("$.data.sourceReference.id").value(quote.sourceReferenceId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long findingId = data(findingResponse).path("id").asLong();

        mockMvc.perform(put("/api/playtest-findings/{id}", findingId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Players now read the timing cue",
                                  "observation": "Cue is clearer after animation update.",
                                  "severity": "LOW",
                                  "recommendation": "Keep the anticipation frame.",
                                  "sourceReferenceId": %d,
                                  "status": "RESOLVED"
                                }
                                """.formatted(quote.sourceReferenceId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(delete("/api/playtest-findings/{id}", findingId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void createPrototypeTaskFromDailyPromptProducesProjectApplication() throws Exception {
        String token = register("project-daily-%s@bookos.local".formatted(UUID.randomUUID()), "Project Daily");
        Long projectId = createProject(token, "Daily Prompt Project");
        Long dailyPromptId = data(mockMvc.perform(get("/api/daily/today")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prompt.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("prompt").path("id").asLong();

        mockMvc.perform(post("/api/projects/{projectId}/create-prototype-task-from-daily", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dailyDesignPromptId": %d,
                                  "title": "Prototype daily prompt",
                                  "description": "Turn the prompt into a testable prototype task."
                                }
                                """.formatted(dailyPromptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceEntityType").value("DAILY_DESIGN_PROMPT"))
                .andExpect(jsonPath("$.data.sourceEntityId").value(dailyPromptId))
                .andExpect(jsonPath("$.data.applicationType").value("PROTOTYPE_TASK"));
    }

    @Test
    void projectIntegrationsSearchGraphForumAndDailyLensReviewAreOwnerScoped() throws Exception {
        String ownerToken = register("proj-int-owner-%s@example.test".formatted(UUID.randomUUID()), "Project Integration Owner");
        String intruderToken = register("proj-int-intruder-%s@example.test".formatted(UUID.randomUUID()), "Project Integration Intruder");
        Long projectId = createProject(ownerToken, "Private Project Search Graph Target");
        Long problemId = createProblem(ownerToken, projectId, "Readable onboarding problem");
        Long dailyPromptId = data(mockMvc.perform(get("/api/daily/today")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("prompt").path("id").asLong();

        mockMvc.perform(get("/api/search")
                        .header("Authorization", bearer(ownerToken))
                        .queryParam("q", "Private Project Search Graph Target")
                        .queryParam("type", "GAME_PROJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("GAME_PROJECT"))
                .andExpect(jsonPath("$.data[0].projectId").value(projectId));

        mockMvc.perform(get("/api/search")
                        .header("Authorization", bearer(intruderToken))
                        .queryParam("q", "Private Project Search Graph Target"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mockMvc.perform(get("/api/graph/project/{projectId}", projectId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[?(@.type == 'PROJECT')]").isNotEmpty())
                .andExpect(jsonPath("$.data.nodes[?(@.type == 'PROJECT_PROBLEM')]").isNotEmpty())
                .andExpect(jsonPath("$.data.edges[?(@.type == 'EVIDENCE_FOR')]").isNotEmpty());

        mockMvc.perform(get("/api/graph/project/{projectId}", projectId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());

        Long categoryId = projectCritiqueCategoryId(ownerToken);
        String threadResponse = mockMvc.perform(post("/api/forum/threads")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Critique private project",
                                  "bodyMarkdown": "Discuss the private project without leaking it.",
                                  "relatedEntityType": "GAME_PROJECT",
                                  "relatedEntityId": %d,
                                  "relatedProjectId": %d,
                                  "visibility": "SHARED"
                                }
                                """.formatted(categoryId, projectId, projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.relatedProjectId").value(projectId))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long threadId = data(threadResponse).path("id").asLong();

        mockMvc.perform(get("/api/forum/threads/{id}", threadId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.relatedProjectId").isEmpty())
                .andExpect(jsonPath("$.data.sourceContextUnavailable").value(true));

        mockMvc.perform(post("/api/projects/{projectId}/lens-reviews", projectId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "Daily prompt #%d as a project review",
                                  "answer": "Use the daily prompt to review onboarding.",
                                  "score": 6,
                                  "status": "OPEN"
                                }
                                """.formatted(dailyPromptId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.question").value("Daily prompt #%d as a project review".formatted(dailyPromptId)));

        mockMvc.perform(get("/api/search")
                        .header("Authorization", bearer(ownerToken))
                        .queryParam("q", "Readable onboarding problem")
                        .queryParam("type", "PROJECT_PROBLEM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(problemId))
                .andExpect(jsonPath("$.data[0].projectId").value(projectId));
    }

    private Long createProject(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/projects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "A source-backed project.",
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
        return data(response).path("id").asLong();
    }

    private Long createProblem(String token, Long projectId, String title) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/problems", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Problem created for project integration testing.",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
    }

    private Long projectCritiqueCategoryId(String token) throws Exception {
        JsonNode categories = data(mockMvc.perform(get("/api/forum/categories")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        for (JsonNode category : categories) {
            if ("project-critiques".equals(category.path("slug").asText())) {
                return category.path("id").asLong();
            }
        }
        throw new IllegalStateException("Project Critiques category not found.");
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Project Mode Test Book %s",
                                  "subtitle": "Source-backed project mode",
                                  "description": "Original test metadata only.",
                                  "publisher": "BookOS Test",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/project-mode.jpg",
                                  "category": "Game Design",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Tests"],
                                  "tags": ["project-mode"]
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

    private QuoteFixture createSourceBackedQuote(String token, Long bookId, String quoteText) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCAC p.77 %s #quote [[Project Mode Concept]]"
                                }
                                """.formatted(bookId, quoteText)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long captureId = data(captureResponse).path("id").asLong();

        String convertResponse = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long quoteId = data(convertResponse).path("targetId").asLong();

        String quoteResponse = mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceReference.pageStart").value(77))
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
                                  "name": "Project Mode Concept %s",
                                  "description": "Original concept description for project tests.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "ontologyLayer": "Projects & Application",
                                  "tags": ["project-mode"]
                                }
                                """.formatted(UUID.randomUUID(), bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
    }

    private Long createKnowledgeObject(String token, Long bookId, Long conceptId, Long sourceReferenceId) throws Exception {
        String response = mockMvc.perform(post("/api/knowledge-objects")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DESIGN_LENS",
                                  "title": "Project Mode Lens %s",
                                  "description": "Original design lens for project tests.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "conceptId": %d,
                                  "sourceReferenceId": %d,
                                  "ontologyLayer": "Design Lenses",
                                  "tags": ["lens", "project-mode"]
                                }
                                """.formatted(UUID.randomUUID(), bookId, conceptId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
    }

    private Long createPlaytestPlan(String token, Long projectId) throws Exception {
        String response = mockMvc.perform(post("/api/projects/{projectId}/playtest-plans", projectId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First loop test",
                                  "hypothesis": "Players understand the jump timing.",
                                  "targetPlayers": "3 designers",
                                  "tasks": "Run the first room twice.",
                                  "successCriteria": "Players state the timing rule.",
                                  "status": "DRAFT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(response).path("id").asLong();
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
