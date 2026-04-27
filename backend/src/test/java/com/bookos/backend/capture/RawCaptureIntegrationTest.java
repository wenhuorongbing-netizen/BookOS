package com.bookos.backend.capture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class RawCaptureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanSaveReparseArchiveAndConvertRawCaptures() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        String createResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCA1 p.12 This could become a core loop [[Core Loop]] #prototype"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("INBOX"))
                .andExpect(jsonPath("$.data.parsedType").value("INSPIRATION"))
                .andExpect(jsonPath("$.data.pageStart").value(12))
                .andExpect(jsonPath("$.data.tags[0]").value("prototype"))
                .andExpect(jsonPath("$.data.concepts[0]").value("Core Loop"))
                .andExpect(jsonPath("$.data.sourceReferences[0].rawCaptureId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long captureId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/captures/inbox")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(captureId)).exists());

        mockMvc.perform(put("/api/captures/{id}", captureId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawText": "\\u2705 \\u7B2C80\\u9875 Test this feedback loop tomorrow. #todo [[Feedback Loop]]"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parsedType").value("ACTION_ITEM"))
                .andExpect(jsonPath("$.data.pageStart").value(80))
                .andExpect(jsonPath("$.data.sourceReferences[0].sourceConfidence").value("HIGH"));

        String conversionResponse = mockMvc.perform(post("/api/captures/{id}/convert", captureId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetType": "NOTE",
                                  "title": "Converted feedback loop capture"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("NOTE"))
                .andExpect(jsonPath("$.data.capture.status").value("CONVERTED"))
                .andExpect(jsonPath("$.data.capture.convertedEntityType").value("NOTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long noteId = objectMapper.readTree(conversionResponse).path("data").path("targetId").asLong();

        mockMvc.perform(get("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Converted feedback loop capture"));

        mockMvc.perform(get("/api/captures/inbox")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(captureId)).doesNotExist());

        String archiveCandidate = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\u2753 p.91 What does this imply for player motivation? [[Motivation]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long archiveCaptureId = objectMapper.readTree(archiveCandidate).path("data").path("id").asLong();

        mockMvc.perform(put("/api/captures/{id}/archive", archiveCaptureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Quick Capture Integration Book",
                                  "subtitle": "A Phase 4 Test Book",
                                  "description": "Testing persisted captures.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/quick-capture.jpg",
                                  "category": "Knowledge Workflow",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["capture", "inbox", "notes"]
                                }
                                """))
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
}
