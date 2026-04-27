package com.bookos.backend.note;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class BookNoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanCreateParsedBookNoteAndManageBlocks() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        mockMvc.perform(post("/api/parser/preview")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawText": "\\uD83D\\uDCAC p.42 Source-backed idea. #quote [[Source Reference]]"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("QUOTE"))
                .andExpect(jsonPath("$.data.pageStart").value(42))
                .andExpect(jsonPath("$.data.tags[0]").value("quote"))
                .andExpect(jsonPath("$.data.concepts[0]").value("Source Reference"));

        String createNoteResponse = mockMvc.perform(post("/api/books/{bookId}/notes", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Source-backed quote",
                                  "markdown": "\\uD83D\\uDCAC p.42 Source-backed idea. #quote [[Source Reference]]",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Source-backed quote"))
                .andExpect(jsonPath("$.data.blocks[0].blockType").value("QUOTE"))
                .andExpect(jsonPath("$.data.blocks[0].pageStart").value(42))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long noteId = objectMapper.readTree(createNoteResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocks[0].sourceReferences[0].bookId").value(bookId))
                .andExpect(jsonPath("$.data.blocks[0].sourceReferences[0].pageStart").value(42))
                .andExpect(jsonPath("$.data.blocks[0].sourceReferences[0].sourceConfidence").value("HIGH"));

        String addBlockResponse = mockMvc.perform(post("/api/notes/{id}/blocks", noteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawText": "\\u2705 \\u7B2C80\\u9875 Test this feedback loop tomorrow. #todo [[Feedback Loop]]",
                                  "sortOrder": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.blockType").value("ACTION_ITEM"))
                .andExpect(jsonPath("$.data.pageStart").value(80))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long blockId = objectMapper.readTree(addBlockResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/note-blocks/{id}", blockId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawText": "\\u2753 page 81 What is the feedback cost? #question [[Feedback Loop]]",
                                  "sortOrder": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockType").value("QUESTION"))
                .andExpect(jsonPath("$.data.pageStart").value(81));

        mockMvc.perform(put("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated source-backed note",
                                  "markdown": "\\uD83D\\uDCA1 p.43 Updated note body. [[Iteration]]",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated source-backed note"));

        mockMvc.perform(get("/api/books/{bookId}/notes", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(noteId)).exists());

        mockMvc.perform(delete("/api/notes/{id}", noteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books/{bookId}/notes", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(noteId)).doesNotExist());
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Parser Integration Book",
                                  "subtitle": "A Phase 3 Test Book",
                                  "description": "Testing deterministic parser and source references.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/parser-integration.jpg",
                                  "category": "Knowledge Systems",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["parser", "notes", "source"]
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
