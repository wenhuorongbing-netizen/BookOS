package com.bookos.backend.learning.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ReviewItemResponse(
        Long id,
        Long reviewSessionId,
        String targetType,
        Long targetId,
        SourceReferenceResponse sourceReference,
        String prompt,
        String userResponse,
        String status,
        Integer confidenceScore,
        Instant createdAt,
        Instant updatedAt) {}
