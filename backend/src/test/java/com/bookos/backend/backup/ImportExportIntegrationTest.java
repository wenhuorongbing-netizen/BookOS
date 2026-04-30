package com.bookos.backend.backup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
class ImportExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportIsUserScopedAndJsonBackupCanRoundtripWithSourceReferences() throws Exception {
        String ownerToken = register("export-owner-%s@example.test".formatted(UUID.randomUUID()), "Export Owner");
        String importerToken = register("export-importer-%s@example.test".formatted(UUID.randomUUID()), "Export Importer");
        Long ownerBookId = createBookAndAddToLibrary(ownerToken, "Export Source Book " + UUID.randomUUID());

        mockMvc.perform(post("/api/books/{bookId}/notes", ownerBookId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Source-backed note",
                                  "markdown": "\\uD83D\\uDCA1 p.9 A readable exported note. [[Export Concept]] #export",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated());
        Long quoteId = createSourceBackedQuote(ownerToken, ownerBookId);

        String exportResponse = mockMvc.perform(get("/api/export/json")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.books[0].title").exists())
                .andExpect(jsonPath("$.data.quotes[?(@.id == %s)]".formatted(quoteId)).exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode exportData = data(exportResponse);

        mockMvc.perform(get("/api/export/json")
                        .header("Authorization", bearer(importerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quotes[?(@.id == %s)]".formatted(quoteId)).doesNotExist());

        String importContent = objectMapper.writeValueAsString(exportData);
        mockMvc.perform(post("/api/import/preview")
                        .header("Authorization", bearer(importerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "importType", "BOOKOS_JSON",
                                "content", importContent,
                                "fileName", "bookos-export.json"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordsToCreate").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(post("/api/import/commit")
                        .header("Authorization", bearer(importerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "importType", "BOOKOS_JSON",
                                "content", importContent,
                                "fileName", "bookos-export.json"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.booksCreated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.quotesCreated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/quotes")
                        .header("Authorization", bearer(importerToken))
                        .param("q", "Exported source-backed quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pageStart").value(12))
                .andExpect(jsonPath("$.data[0].sourceReference.sourceConfidence").value("HIGH"));
    }

    @Test
    void markdownPreviewDoesNotWriteAndCommitDetectsDuplicatesWithoutInventingPages() throws Exception {
        String token = register("markdown-importer-%s@example.test".formatted(UUID.randomUUID()), "Markdown Importer");
        String title = "Markdown Import Book " + UUID.randomUUID();
        String markdown = """
                # %s

                ## Notes

                \\uD83D\\uDCA1 This note has no page number. #import [[Safe Import]]
                """.formatted(title);
        String payload = objectMapper.writeValueAsString(Map.of(
                "importType", "MARKDOWN_NOTES",
                "bookTitle", title,
                "content", markdown,
                "fileName", "markdown-import.md"));

        mockMvc.perform(post("/api/import/preview")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordsToCreate").value(2));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearer(token))
                        .param("q", title))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").doesNotExist());

        mockMvc.perform(post("/api/import/commit")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.booksCreated").value(1))
                .andExpect(jsonPath("$.data.notesCreated").value(1));

        mockMvc.perform(post("/api/import/preview")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.potentialDuplicates").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        String quotesCsv = """
                bookTitle,text,pageStart,pageEnd
                %s,"CSV quote with unknown page",not-a-page,
                """.formatted(title);
        mockMvc.perform(post("/api/import/preview")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "importType", "QUOTES_CSV",
                                "content", quotesCsv,
                                "fileName", "quotes.csv"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageNumberIssues[0]").exists());
    }

    @Test
    void bookMarkdownAndCsvExportsAreReadable() throws Exception {
        String token = register("markdown-exporter-%s@example.test".formatted(UUID.randomUUID()), "Markdown Exporter");
        Long bookId = createBookAndAddToLibrary(token, "Markdown Export Book " + UUID.randomUUID());
        createSourceBackedQuote(token, bookId);

        String markdown = mockMvc.perform(get("/api/export/book/{bookId}/markdown", bookId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(markdown)
                .contains("# Markdown Export Book")
                .contains("## Quotes")
                .contains("## Source References");

        String csv = mockMvc.perform(get("/api/export/quotes/csv")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(csv).contains("bookTitle").contains("Exported source-backed quote");
    }

    @Test
    void bookJsonActionCsvConceptCsvAndCrossUserBookExportAreScoped() throws Exception {
        String ownerToken = register("export-scope-owner-%s@example.test".formatted(UUID.randomUUID()), "Export Scope Owner");
        String intruderToken = register("export-scope-intruder-%s@example.test".formatted(UUID.randomUUID()), "Export Scope Intruder");
        Long bookId = createBookAndAddToLibrary(ownerToken, "Scoped Export Book " + UUID.randomUUID());
        Long quoteId = createSourceBackedQuote(ownerToken, bookId);
        Long sourceReferenceId = quoteSourceReferenceId(ownerToken, quoteId);
        String actionTitle = "Scoped exported action " + UUID.randomUUID();
        String conceptName = "Scoped Export Concept " + UUID.randomUUID();

        mockMvc.perform(post("/api/action-items")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "title": "%s",
                                  "description": "Action item with source reference.",
                                  "priority": "HIGH",
                                  "sourceReferenceId": %d,
                                  "visibility": "PRIVATE"
                                }
                                """.formatted(bookId, actionTitle, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sourceReference.id").value(sourceReferenceId));

        mockMvc.perform(post("/api/concepts")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Concept exported from a source-backed book.",
                                  "visibility": "PRIVATE",
                                  "bookId": %d,
                                  "sourceReferenceId": %d,
                                  "ontologyLayer": "Projects & Application",
                                  "tags": ["export", "scope"]
                                }
                                """.formatted(conceptName, bookId, sourceReferenceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstSourceReference.id").value(sourceReferenceId));

        mockMvc.perform(get("/api/export/book/{bookId}/json", bookId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.books[0].id").value(bookId))
                .andExpect(jsonPath("$.data.quotes[0].sourceReferenceId").value(sourceReferenceId))
                .andExpect(jsonPath("$.data.actionItems[0].title").value(actionTitle))
                .andExpect(jsonPath("$.data.concepts[0].name").value(conceptName))
                .andExpect(jsonPath("$.data.sourceReferences[0].id").value(sourceReferenceId));

        mockMvc.perform(get("/api/export/book/{bookId}/json", bookId)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());

        String actionCsv = mockMvc.perform(get("/api/export/action-items/csv")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(actionCsv)
                .contains("bookTitle")
                .contains(actionTitle)
                .contains(String.valueOf(sourceReferenceId));

        String conceptCsv = mockMvc.perform(get("/api/export/concepts/csv")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(conceptCsv)
                .contains("name")
                .contains(conceptName)
                .contains("Projects & Application");

        String intruderActionCsv = mockMvc.perform(get("/api/export/action-items/csv")
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        org.assertj.core.api.Assertions.assertThat(intruderActionCsv).doesNotContain(actionTitle);
    }

    private Long createBookAndAddToLibrary(String token, String title) throws Exception {
        String response = mockMvc.perform(post("/api/books")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "subtitle": "Import export test",
                                  "description": "Original test metadata only.",
                                  "publisher": "BookOS Press",
                                  "publicationYear": 2026,
                                  "coverUrl": null,
                                  "category": "Import Export",
                                  "visibility": "PRIVATE",
                                  "authors": ["BookOS Team"],
                                  "tags": ["backup"]
                                }
                                """.formatted(title)))
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

    private Long createSourceBackedQuote(String token, Long bookId) throws Exception {
        String captureResponse = mockMvc.perform(post("/api/captures")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d,
                                  "rawText": "\\uD83D\\uDCAC p.12 Exported source-backed quote. #quote [[Backup Loop]]"
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long captureId = data(captureResponse).path("id").asLong();
        String conversionResponse = mockMvc.perform(post("/api/captures/{id}/convert/quote", captureId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(conversionResponse).path("targetId").asLong();
    }

    private Long quoteSourceReferenceId(String token, Long quoteId) throws Exception {
        String quoteResponse = mockMvc.perform(get("/api/quotes/{id}", quoteId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceReference.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return data(quoteResponse).path("sourceReference").path("id").asLong();
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
}
