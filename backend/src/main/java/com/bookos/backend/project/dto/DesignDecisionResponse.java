package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record DesignDecisionResponse(
        Long id,
        Long projectId,
        String title,
        String decision,
        String rationale,
        String tradeoffs,
        SourceReferenceResponse sourceReference,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
