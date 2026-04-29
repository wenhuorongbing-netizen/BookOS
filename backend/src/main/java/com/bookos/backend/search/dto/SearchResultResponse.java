package com.bookos.backend.search.dto;

import java.time.Instant;

public record SearchResultResponse(
        String type,
        Long id,
        String title,
        String excerpt,
        Long bookId,
        String bookTitle,
        Long projectId,
        String projectTitle,
        Long sourceReferenceId,
        Instant updatedAt) {

    public SearchResultResponse(
            String type,
            Long id,
            String title,
            String excerpt,
            Long bookId,
            String bookTitle,
            Long sourceReferenceId,
            Instant updatedAt) {
        this(type, id, title, excerpt, bookId, bookTitle, null, null, sourceReferenceId, updatedAt);
    }
}
