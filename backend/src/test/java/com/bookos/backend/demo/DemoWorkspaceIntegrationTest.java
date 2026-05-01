package com.bookos.backend.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        String startResponse = mockMvc.perform(post("/api/demo/start").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.lastResetAt").isString())
                .andExpect(jsonPath("$.data.bookId").isNumber())
                .andExpect(jsonPath("$.data.projectId").isNumber())
                .andExpect(jsonPath("$.data.quoteId").isNumber())
                .andExpect(jsonPath("$.data.actionItemId").isNumber())
                .andExpect(jsonPath("$.data.conceptIds.length()").value(4))
                .andExpect(jsonPath("$.data.recordCounts.BOOK").value(1))
                .andExpect(jsonPath("$.data.includedRecordTypes", hasItem("BOOK")))
                .andExpect(jsonPath("$.data.includedRecordTypes", hasItem("GAME_PROJECT")))
                .andExpect(jsonPath("$.data.excludedFromAnalyticsByDefault").value(true))
                .andExpect(jsonPath("$.data.safetyNote").value("Demo mode uses original BookOS sample content, stores unknown pages as null, and labels every created record as demo."))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode startData = objectMapper.readTree(startResponse).path("data");
        Long demoBookId = startData.path("bookId").asLong();
        Long demoProjectId = startData.path("projectId").asLong();

        mockMvc.perform(get("/api/demo/status").header("Authorization", bearer(intruderToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        String sourcesResponse = mockMvc.perform(get("/api/source-references")
                        .queryParam("bookId", demoBookId.toString())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(5)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode sources = objectMapper.readTree(sourcesResponse).path("data");
        assertThat(StreamSupport.stream(sources.spliterator(), false)
                        .allMatch(source -> source.path("pageStart").isNull() && source.path("pageEnd").isNull()))
                .isTrue();
        assertThat(StreamSupport.stream(sources.spliterator(), false)
                        .allMatch(source -> "LOW".equals(source.path("sourceConfidence").asText())))
                .isTrue();
        assertThat(sources.toString())
                .doesNotContain("The impediment to action")
                .doesNotContain("Discipline equals freedom");

        mockMvc.perform(get("/api/search")
                        .queryParam("q", "Demo Game Design Notebook")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        String graphResponse = mockMvc.perform(get("/api/graph/book/{bookId}", demoBookId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes.length()", greaterThanOrEqualTo(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode graph = objectMapper.readTree(graphResponse).path("data").path("nodes");
        assertThat(StreamSupport.stream(graph.spliterator(), false)
                        .anyMatch(node -> node.path("label").asText().startsWith("[Demo]")))
                .isTrue();

        mockMvc.perform(get("/api/books/{id}", demoBookId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/graph/project/{projectId}", demoProjectId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());

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

        Long realBookId = createRealBookInLibrary(ownerToken, "Real Book " + UUID.randomUUID());

        mockMvc.perform(post("/api/demo/reset").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.recordCounts.RAW_CAPTURE", greaterThanOrEqualTo(4)));
        mockMvc.perform(get("/api/books/{id}", realBookId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").isString());

        mockMvc.perform(delete("/api/demo").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/demo/status").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
        mockMvc.perform(get("/api/books/{id}", realBookId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/reading")
                        .queryParam("includeDemo", "true")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.libraryBooks").value(1));
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

    private Long createRealBookInLibrary(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "A real user-owned book that must survive demo reset and delete.",
                                  "category": "QA",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS QA"],
                                  "tags": ["qa"]
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long bookId = objectMapper.readTree(response).path("data").path("id").asLong();
        mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "readingStatus": "CURRENTLY_READING",
                                  "readingFormat": "OTHER",
                                  "ownershipStatus": "OWNED"
                                }
                                """))
                .andExpect(status().isCreated());
        return bookId;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
