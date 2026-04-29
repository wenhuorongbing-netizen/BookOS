package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ProjectProblemResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        String status,
        String priority,
        SourceReferenceResponse relatedSourceReference,
        Instant createdAt,
        Instant updatedAt) {}
