package com.bookos.backend.source.dto;

import com.bookos.backend.common.enums.SourceConfidence;
import java.time.Instant;

public record SourceReferenceResponse(
        Long id,
        String sourceType,
        Long bookId,
        Long noteId,
        Long noteBlockId,
        Long chapterId,
        Long rawCaptureId,
        Integer pageStart,
        Integer pageEnd,
        String locationLabel,
        String sourceText,
        SourceConfidence sourceConfidence,
        Instant createdAt) {}
