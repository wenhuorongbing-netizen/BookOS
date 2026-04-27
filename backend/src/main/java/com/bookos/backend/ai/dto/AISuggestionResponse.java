package com.bookos.backend.ai.dto;

import com.bookos.backend.common.enums.AISuggestionStatus;
import com.bookos.backend.common.enums.AISuggestionType;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record AISuggestionResponse(
        Long id,
        AISuggestionType suggestionType,
        AISuggestionStatus status,
        String providerName,
        Long bookId,
        String bookTitle,
        Long sourceReferenceId,
        SourceReferenceResponse sourceReference,
        String draftText,
        String draftJson,
        Instant createdAt,
        Instant updatedAt) {}
