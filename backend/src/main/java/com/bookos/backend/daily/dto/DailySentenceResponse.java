package com.bookos.backend.daily.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.time.LocalDate;

public record DailySentenceResponse(
        Long id,
        LocalDate day,
        String text,
        String attribution,
        Long bookId,
        String bookTitle,
        String sourceType,
        Long sourceId,
        Integer pageStart,
        Integer pageEnd,
        boolean sourceBacked,
        boolean skipped,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
