package com.bookos.backend.search.dto;

import java.time.Instant;

public record SearchResultResponse(
        String type,
        Long id,
        String title,
        String excerpt,
        Long bookId,
        String bookTitle,
        Long sourceReferenceId,
        Instant updatedAt) {}
