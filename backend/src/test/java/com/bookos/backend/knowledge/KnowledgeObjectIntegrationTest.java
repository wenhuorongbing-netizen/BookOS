package com.bookos.backend.knowledge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class KnowledgeObjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanQuerySourceReferencesAndBuildSourceBackedKnowledgeObjects() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        String createNoteResponse = mockMvc.perform(post("/api/books/{bookId}/notes", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Meaningful choice note",
                                  "markdown": "\\uD83D\\uDCA1 p.55 Prototype this choice tradeoff. #design [[Meaningful Choice Phase 5A]]",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.blocks[0].blockType").value("INSPIRATION"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long noteId = objectMapper.readTree(createNoteResponse).path("data").path("id").asLong();

        String noteResponse = mockMvc.perform(get("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocks[0].sourceReferences[0].pageStart").value(55))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long sourceReferenceId = objectMapper.readTree(noteResponse)
                .path("data")
                .path("blocks")
                .get(0)
                .path("sourceReferences")
                .get(0)
                .path("id")
                .asLong();

        mockMvc.perform(get("/api/source-references/{id}", sourceReferenceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.pageStart").value(55))
                .andExpect(jsonPath("$.data.sourceConfidence").value("HIGH"));

        mockMvc.perform(get("/api/books/{bookId}/source-references", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(sourceReferenceId)).exists());

        String conceptResponse = mockMvc.perform(get("/api/books/{bookId}/concepts", bookId)
                        .header("Authorization", "Bearer " + token)
                        .param("q", "Meaningful Choice Phase 5A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Meaningful Choice Phase 5A"))
                .andExpect(jsonPath("$.data[0].mentionCount").value(1))
                .andExpect(jsonPath("$.data[0].sourceReferences[0].pageStart").value(55))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long conceptId = objectMapper.readTree(conceptResponse).path("data").get(0).path("id").asLong();

        String knowledgeResponse = mockMvc.perform(post("/api/knowledge-objects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "PRINCIPLE",
                                  "title": "Choices need visible tradeoffs",
                                  "description": "A source-backed original design principle.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "noteId": %d,
                                  "conceptId": %d,
                                  "sourceReferenceId": %d,
                                  "tags": ["choice", "prototype"]
                                }
                                """.formatted(bookId, noteId, conceptId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReference.pageStart").value(55))
                .andExpect(jsonPath("$.data.conceptId").value(conceptId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long knowledgeObjectId = objectMapper.readTree(knowledgeResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/entity-links")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceType": "CONCEPT",
                                  "sourceId": %d,
                                  "targetType": "KNOWLEDGE_OBJECT",
                                  "targetId": %d,
                                  "relationType": "SUPPORTS",
                                  "sourceReferenceId": %d
                                }
                                """.formatted(conceptId, knowledgeObjectId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.relationType").value("SUPPORTS"));

        mockMvc.perform(get("/api/entity-links")
                        .header("Authorization", "Bearer " + token)
                        .param("sourceType", "CONCEPT")
                        .param("sourceId", String.valueOf(conceptId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.targetId == %s)]".formatted(knowledgeObjectId)).exists());

        mockMvc.perform(get("/api/knowledge-objects")
                        .header("Authorization", "Bearer " + token)
                        .param("conceptId", String.valueOf(conceptId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(knowledgeObjectId)).exists());

        String intruderToken = register("source-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Source Intruder");

        mockMvc.perform(get("/api/source-references/{id}", sourceReferenceId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/concepts/{id}", conceptId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/knowledge-objects/{id}", knowledgeObjectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rawCaptureConceptsAreIndexedWithSourceReferences() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        String createCaptureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83E\\uDDE9 p.12 Connect this to [[Core Loop Phase 5A]] #systems"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReferences[0].pageStart").value(12))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode capture = objectMapper.readTree(createCaptureResponse).path("data");
        Long captureId = capture.path("id").asLong();
        Long sourceReferenceId = capture.path("sourceReferences").get(0).path("id").asLong();

        mockMvc.perform(get("/api/captures/{captureId}/source-references", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(sourceReferenceId)).exists());

        mockMvc.perform(get("/api/concepts")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId))
                        .param("q", "Core Loop Phase 5A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Core Loop Phase 5A"))
                .andExpect(jsonPath("$.data[0].sourceReferences[0].rawCaptureId").value(captureId));
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Phase 5A Knowledge Book %s",
                                  "subtitle": "Source-backed knowledge test",
                                  "description": "Testing source references and knowledge objects.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/phase-5a.jpg",
                                  "category": "Knowledge Systems",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["phase-5a", "knowledge"]
                                }
                                """.formatted(UUID.randomUUID())))
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
}
