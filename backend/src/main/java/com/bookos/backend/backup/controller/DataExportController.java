package com.bookos.backend.backup.controller;

import com.bookos.backend.backup.service.ImportExportService;
import com.bookos.backend.common.ApiResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class DataExportController {

    private final ImportExportService importExportService;

    @GetMapping("/api/export/json")
    public ApiResponse<Map<String, Object>> exportJson(Authentication authentication) {
        return ApiResponse.ok("Export package created.", importExportService.exportAllJson(authentication.getName()));
    }

    @GetMapping("/api/export/book/{bookId}/json")
    public ApiResponse<Map<String, Object>> exportBookJson(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Book export package created.", importExportService.exportBookJson(authentication.getName(), bookId));
    }

    @GetMapping(value = "/api/export/book/{bookId}/markdown", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> exportBookMarkdown(Authentication authentication, @PathVariable Long bookId) {
        return textDownload(
                importExportService.exportBookMarkdown(authentication.getName(), bookId),
                "bookos-book-" + bookId + ".md",
                MediaType.parseMediaType("text/markdown;charset=UTF-8"));
    }

    @GetMapping(value = "/api/export/quotes/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportQuotesCsv(Authentication authentication) {
        return textDownload(importExportService.exportQuotesCsv(authentication.getName()), "bookos-quotes.csv", MediaType.parseMediaType("text/csv;charset=UTF-8"));
    }

    @GetMapping(value = "/api/export/action-items/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportActionItemsCsv(Authentication authentication) {
        return textDownload(importExportService.exportActionItemsCsv(authentication.getName()), "bookos-action-items.csv", MediaType.parseMediaType("text/csv;charset=UTF-8"));
    }

    @GetMapping(value = "/api/export/concepts/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportConceptsCsv(Authentication authentication) {
        return textDownload(importExportService.exportConceptsCsv(authentication.getName()), "bookos-concepts.csv", MediaType.parseMediaType("text/csv;charset=UTF-8"));
    }

    private ResponseEntity<String> textDownload(String content, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(content);
    }
}
