package com.bookos.backend.forum;

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
class ForumIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanCreateSourceBackedThreadCommentLikeBookmarkReportAndDeleteOwnContent() throws Exception {
        String token = loginAsDesigner();
        Long categoryId = firstCategoryId(token);
        Long bookId = createBookAndAddToLibrary(token);
        Long sourceReferenceId = createCaptureSource(token, bookId);

        String createdThread = mockMvc.perform(post("/api/forum/threads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "How should this source change a prototype?",
                                  "bodyMarkdown": "## Context\\n\\n<script>alert('x')</script> Source-backed forum body.",
                                  "relatedEntityType": "BOOK",
                                  "relatedEntityId": %d,
                                  "relatedBookId": %d,
                                  "sourceReferenceId": %d,
                                  "visibility": "SHARED"
                                }
                                """.formatted(categoryId, bookId, bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReference.id").value(sourceReferenceId))
                .andExpect(jsonPath("$.data.bodyMarkdown").value("## Context\n\nalert('x') Source-backed forum body."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long threadId = objectMapper.readTree(createdThread).path("data").path("id").asLong();

        mockMvc.perform(get("/api/forum/threads/{id}", threadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("How should this source change a prototype?"))
                .andExpect(jsonPath("$.data.canEdit").value(true));

        mockMvc.perform(post("/api/forum/threads/{id}/comments", threadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bodyMarkdown": "This should become a small prototype challenge."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bodyMarkdown").value("This should become a small prototype challenge."));

        mockMvc.perform(post("/api/forum/threads/{id}/like", threadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(post("/api/forum/threads/{id}/bookmark", threadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarkedByCurrentUser").value(true));

        mockMvc.perform(post("/api/forum/threads/{id}/report", threadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Needs moderator review",
                                  "details": "Testing report flow."
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/forum/threads")
                        .header("Authorization", "Bearer " + token)
                        .param("q", "prototype"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(threadId)).exists());

        mockMvc.perform(put("/api/forum/threads/{id}", threadId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Updated prototype discussion",
                                  "bodyMarkdown": "Updated source-backed body.",
                                  "relatedEntityType": "BOOK",
                                  "relatedEntityId": %d,
                                  "relatedBookId": %d,
                                  "sourceReferenceId": %d,
                                  "visibility": "SHARED"
                                }
                                """.formatted(categoryId, bookId, bookId, sourceReferenceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated prototype discussion"));

        String intruderToken = register("forum-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Forum Intruder");
        mockMvc.perform(put("/api/forum/threads/{id}", threadId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Intruder update",
                                  "bodyMarkdown": "No.",
                                  "visibility": "SHARED"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/forum/threads/{id}", threadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/forum/threads/{id}", threadId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void defaultCategoriesAndTemplatesExist() throws Exception {
        String token = loginAsDesigner();

        mockMvc.perform(get("/api/forum/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.slug == 'book-discussions')]").exists())
                .andExpect(jsonPath("$.data[?(@.slug == 'prototype-challenges')]").exists());

        mockMvc.perform(get("/api/forum/templates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.slug == 'book-discussion')]").exists())
                .andExpect(jsonPath("$.data[?(@.slug == 'prototype-challenge')]").exists());
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

    private Long createCaptureSource(String token, Long bookId) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCA1 p.88 Discuss this source in forum. #forum [[Forum Source]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode source = objectMapper.readTree(captureResponse).path("data").path("sourceReferences").get(0);
        return source.path("id").asLong();
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Forum MVP Book %s",
                                  "subtitle": "Structured forum test",
                                  "description": "Testing structured forum attachment.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/forum.jpg",
                                  "category": "Forum",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["forum"]
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
