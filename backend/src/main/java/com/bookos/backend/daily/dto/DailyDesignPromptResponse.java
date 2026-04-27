package com.bookos.backend.daily.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.time.LocalDate;

public record DailyDesignPromptResponse(
        Long id,
        LocalDate day,
        String question,
        String sourceTitle,
        Long bookId,
        String bookTitle,
        String sourceType,
        Long sourceId,
        Long knowledgeObjectId,
        boolean templatePrompt,
        boolean skipped,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
