package com.bookos.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
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
class OntologyImportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanImportOntologySeedIdempotentlyWithoutFakePages() throws Exception {
        String token = login("admin@bookos.local", "Admin123!");
        String suffix = UUID.randomUUID().toString();
        String bookTitle = "Seed Source Book " + suffix;
        String conceptTitle = "Seed Concept " + suffix;
        String objectTitle = "Seed Lens " + suffix;
        String payload = payload(bookTitle, conceptTitle, objectTitle);

        mockMvc.perform(post("/api/admin/ontology/import")
                        .header("Authorization", "Bearer " + token)
                        .param("dryRun", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dryRun").value(true))
                .andExpect(jsonPath("$.data.conceptsCreated").value(1));

        JsonNode importResult = objectMapper.readTree(mockMvc.perform(post("/api/admin/ontology/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dryRun").value(false))
                .andExpect(jsonPath("$.data.booksCreated").value(1))
                .andExpect(jsonPath("$.data.conceptsCreated").value(1))
                .andExpect(jsonPath("$.data.knowledgeObjectsCreated").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(importResult.path("sourceReferencesCreated").asInt()).isGreaterThanOrEqualTo(1);

        mockMvc.perform(post("/api/admin/ontology/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conceptsCreated").value(0))
                .andExpect(jsonPath("$.data.knowledgeObjectsCreated").value(0));

        JsonNode concepts = objectMapper.readTree(mockMvc.perform(get("/api/concepts")
                        .header("Authorization", "Bearer " + token)
                        .param("q", conceptTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value(conceptTitle))
                .andExpect(jsonPath("$.data[0].ontologyLayer").value("Systems & Loops"))
                .andExpect(jsonPath("$.data[0].createdBy").value("SYSTEM"))
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(concepts.get(0).path("firstSourceReference").path("pageStart").isNull()).isTrue();
        long conceptId = concepts.get(0).path("id").asLong();

        JsonNode objects = objectMapper.readTree(mockMvc.perform(get("/api/knowledge-objects")
                        .header("Authorization", "Bearer " + token)
                        .param("q", objectTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value(objectTitle))
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(objects.get(0).path("sourceReference").path("pageStart").isNull()).isTrue();
        long knowledgeObjectId = objects.get(0).path("id").asLong();

        JsonNode search = objectMapper.readTree(mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + token)
                        .param("q", conceptTitle))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(StreamSupport.stream(search.spliterator(), false)
                        .anyMatch(node -> "CONCEPT".equals(node.path("type").asText())
                                && conceptTitle.equals(node.path("title").asText())))
                .isTrue();

        JsonNode graph = objectMapper.readTree(mockMvc.perform(get("/api/graph/concept/{conceptId}", conceptId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).path("data");
        assertThat(StreamSupport.stream(graph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "CONCEPT".equals(node.path("type").asText())
                                && conceptId == node.path("entityId").asLong()))
                .isTrue();
        assertThat(StreamSupport.stream(graph.path("nodes").spliterator(), false)
                        .anyMatch(node -> "KNOWLEDGE_OBJECT".equals(node.path("type").asText())
                                && knowledgeObjectId == node.path("entityId").asLong()))
                .isTrue();
        assertThat(StreamSupport.stream(graph.path("edges").spliterator(), false)
                        .anyMatch(edge -> "RELATED_TO".equals(edge.path("type").asText())))
                .isTrue();
    }

    @Test
    void nonAdminCannotImportOntologySeed() throws Exception {
        String token = login("designer@bookos.local", "Password123!");
        mockMvc.perform(post("/api/admin/ontology/import/default")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String payload(String bookTitle, String conceptTitle, String objectTitle) {
        return """
                {
                  "books": [
                    {
                      "title": "%s",
                      "summary": "Original seed-only metadata summary for a test book.",
                      "category": "Systems & Loops",
                      "visibility": "PUBLIC",
                      "authors": ["BookOS Test"],
                      "tags": ["seed", "systems"]
                    }
                  ],
                  "concepts": [
                    {
                      "title": "%s",
                      "description": "Original test concept description for import validation.",
                      "layer": "Systems & Loops",
                      "tags": ["seed", "loop"],
                      "sourceBookTitle": "%s",
                      "sourceConfidence": "LOW"
                    }
                  ],
                  "knowledgeObjects": [
                    {
                      "type": "DESIGN_LENS",
                      "title": "%s",
                      "description": "Original design lens prompt for import validation.",
                      "layer": "Systems & Loops",
                      "tags": ["seed", "lens"],
                      "sourceBookTitle": "%s",
                      "conceptTitle": "%s",
                      "sourceConfidence": "LOW"
                    }
                  ]
                }
                """.formatted(bookTitle, conceptTitle, bookTitle, objectTitle, bookTitle, conceptTitle);
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
