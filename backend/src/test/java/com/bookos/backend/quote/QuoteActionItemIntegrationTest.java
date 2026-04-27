package com.bookos.backend.quote;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class QuoteActionItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCanConvertCaptureToSourceBackedQuote() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCAC p.112 The jump arc communicates intent. #quote [[Game Feel Phase 5B]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parsedType").value("QUOTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long captureId = objectMapper.readTree(captureResponse).path("data").path("id").asLong();

        String conversionResponse = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("QUOTE"))
                .andExpect(jsonPath("$.data.capture.convertedEntityType").value("QUOTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long quoteId = objectMapper.readTree(conversionResponse).path("data").path("targetId").asLong();

        mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.rawCaptureId").value(captureId))
                .andExpect(jsonPath("$.data.pageStart").value(112))
                .andExpect(jsonPath("$.data.sourceReference.rawCaptureId").value(captureId))
                .andExpect(jsonPath("$.data.sourceReference.sourceConfidence").value("HIGH"));

        mockMvc.perform(get("/api/quotes")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId))
                        .param("q", "jump arc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(quoteId)).exists());

        String intruderToken = register("quote-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Quote Intruder");
        mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCanConvertCaptureToSourceBackedActionItemAndCompleteIt() throws Exception {
        String token = loginAsDesigner();
        Long bookId = createBookAndAddToLibrary(token);

        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\u2705 p.45 Test the feedback loop tomorrow. #todo [[Feedback Loop Phase 5B]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parsedType").value("ACTION_ITEM"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long captureId = objectMapper.readTree(captureResponse).path("data").path("id").asLong();

        String conversionResponse = mockMvc.perform(post("/api/captures/{id}/convert/action-item", captureId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test the feedback loop tomorrow"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("ACTION_ITEM"))
                .andExpect(jsonPath("$.data.capture.convertedEntityType").value("ACTION_ITEM"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long actionItemId = objectMapper.readTree(conversionResponse).path("data").path("targetId").asLong();

        mockMvc.perform(get("/api/action-items/{id}", actionItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.rawCaptureId").value(captureId))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.pageStart").value(45))
                .andExpect(jsonPath("$.data.sourceReference.rawCaptureId").value(captureId));

        mockMvc.perform(put("/api/action-items/{id}/complete", actionItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());

        mockMvc.perform(get("/api/action-items")
                        .header("Authorization", "Bearer " + token)
                        .param("bookId", String.valueOf(bookId))
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == %s)]".formatted(actionItemId)).exists());

        mockMvc.perform(put("/api/action-items/{id}/reopen", actionItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(false));

        String intruderToken = register("action-intruder-%s@bookos.local".formatted(UUID.randomUUID()), "Action Intruder");
        mockMvc.perform(get("/api/action-items/{id}", actionItemId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
    }

    private Long createBookAndAddToLibrary(String token) throws Exception {
        String createBookResponse = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Phase 5B Quote Action Book %s",
                                  "subtitle": "Source-backed quote action test",
                                  "description": "Testing quotes and action items.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": "https://example.com/covers/phase-5b.jpg",
                                  "category": "Knowledge Actions",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["phase-5b", "quotes", "actions"]
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
