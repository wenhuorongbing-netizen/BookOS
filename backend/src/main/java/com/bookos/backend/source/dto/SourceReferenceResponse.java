package com.bookos.backend.source.dto;

import com.bookos.backend.common.enums.SourceConfidence;

public record SourceReferenceResponse(
        Long id,
        String sourceType,
        Long bookId,
        Long noteId,
        Long noteBlockId,
        Long rawCaptureId,
        Integer pageStart,
        Integer pageEnd,
        String locationLabel,
        String sourceText,
        SourceConfidence sourceConfidence) {}
