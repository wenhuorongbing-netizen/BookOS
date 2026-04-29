package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ProjectApplicationResponse(
        Long id,
        Long projectId,
        String sourceEntityType,
        Long sourceEntityId,
        SourceReferenceResponse sourceReference,
        String applicationType,
        String title,
        String description,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
