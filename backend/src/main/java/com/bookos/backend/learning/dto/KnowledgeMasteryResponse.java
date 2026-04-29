package com.bookos.backend.learning.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record KnowledgeMasteryResponse(
        Long id,
        String targetType,
        Long targetId,
        Integer familiarityScore,
        Integer usefulnessScore,
        Instant lastReviewedAt,
        Instant nextReviewAt,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
