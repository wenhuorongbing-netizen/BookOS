package com.bookos.backend.action.dto;

import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ActionItemResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long noteId,
        Long noteBlockId,
        Long rawCaptureId,
        String title,
        String description,
        ActionPriority priority,
        Integer pageStart,
        Integer pageEnd,
        boolean completed,
        Instant completedAt,
        Visibility visibility,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
