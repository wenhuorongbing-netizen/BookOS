package com.bookos.backend.quote.dto;

import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.util.List;

public record QuoteResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long noteId,
        Long noteBlockId,
        Long rawCaptureId,
        String text,
        String attribution,
        Integer pageStart,
        Integer pageEnd,
        List<String> tags,
        List<String> concepts,
        Visibility visibility,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
