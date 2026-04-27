package com.bookos.backend.capture.dto;

import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.util.List;

public record RawCaptureResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String rawText,
        String cleanText,
        NoteBlockType parsedType,
        Integer pageStart,
        Integer pageEnd,
        List<String> tags,
        List<String> concepts,
        List<String> parserWarnings,
        CaptureStatus status,
        String convertedEntityType,
        Long convertedEntityId,
        List<SourceReferenceResponse> sourceReferences,
        Instant createdAt,
        Instant updatedAt) {}
