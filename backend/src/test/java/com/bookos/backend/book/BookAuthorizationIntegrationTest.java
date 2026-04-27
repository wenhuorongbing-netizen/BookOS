package com.bookos.backend.book;

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
class BookAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void privateBookIsOnlyVisibleAndMutableByOwnerOrAdmin() throws Exception {
        String ownerToken = register("book-owner@bookos.local", "Book Owner");
        String intruderToken = register("book-intruder@bookos.local", "Book Intruder");
        String adminToken = login("admin@bookos.local", "Admin123!");

        String privateBookBody = """
                {
                  "title": "Private Combat Notebook",
                  "subtitle": "Owner-only source notes",
                  "description": "A private book record for authorization testing.",
                  "publisher": "BookOS Press",
                  "publicationYear": 2026,
                  "coverUrl": "https://example.com/covers/private-combat.jpg",
                  "category": "Private Research",
                  "visibility": "PRIVATE",
                  "authors": ["BookOS Owner"],
                  "tags": ["private", "combat"]
                }
                """;

        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privateBookBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Private Combat Notebook"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookId = objectMapper.readTree(createBookResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/books/{id}", bookId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(bookId));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(bookId)).doesNotExist());

        mockMvc.perform(get("/api/books/{id}", bookId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/books/{id}/add-to-library", bookId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privateBookBody.replace("Private Combat Notebook", "Intruder Rename")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/books/{id}", bookId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(privateBookBody.replace("Private Combat Notebook", "Admin Reviewed Notebook")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Admin Reviewed Notebook"));
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
