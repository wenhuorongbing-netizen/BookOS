package com.bookos.backend.learning.dto;

import java.time.Instant;

public record ReadingSessionResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Instant startedAt,
        Instant endedAt,
        Integer startPage,
        Integer endPage,
        Integer minutesRead,
        Integer notesCount,
        Integer capturesCount,
        String reflection,
        Instant createdAt,
        Instant updatedAt) {}
