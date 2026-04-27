package com.bookos.backend.book;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
class BookLibraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanCreateBookAddItToLibraryAndSeeItInFiveStarView() throws Exception {
        String token = loginAsDesigner();

        String createBookBody = """
                {
                  "title": "Designing Combat Feedback",
                  "subtitle": "A BookOS Test Book",
                  "description": "Testing the milestone 1 library flow.",
                  "publisher": "BookOS Press",
                  "publicationYear": 2026,
                  "coverUrl": "https://example.com/covers/combat-feedback.jpg",
                  "category": "Experience Design",
                  "visibility": "PRIVATE",
                  "authors": ["BookOS Team"],
                  "tags": ["feedback", "combat", "prototype"]
                }
                """;

        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Designing Combat Feedback"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(createBookResponse).path("data").path("id").asLong();

        String addResponse = mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode userBookJson = objectMapper.readTree(addResponse).path("data");
        Long userBookId = userBookJson.path("id").asLong();

        mockMvc.perform(put("/api/user-books/{id}/status", userBookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "CURRENTLY_READING" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readingStatus").value("CURRENTLY_READING"));

        mockMvc.perform(put("/api/user-books/{id}/progress", userBookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "progressPercent": 47 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(47));

        mockMvc.perform(put("/api/user-books/{id}/rating", userBookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "rating": 5 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5));

        mockMvc.perform(get("/api/user-books/currently-reading")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)].readingStatus".formatted(userBookId)).exists());

        mockMvc.perform(get("/api/user-books/five-star")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)].rating".formatted(userBookId)).exists());
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
