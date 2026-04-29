package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record PlaytestFindingResponse(
        Long id,
        Long projectId,
        Long sessionId,
        String title,
        String observation,
        String severity,
        String recommendation,
        SourceReferenceResponse sourceReference,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
