package com.bookos.backend.learning.dto;

import java.time.Instant;
import java.util.List;

public record ReviewSessionResponse(
        Long id,
        String title,
        Instant startedAt,
        Instant completedAt,
        String mode,
        String scopeType,
        Long scopeId,
        String summary,
        int itemCount,
        int completedItemCount,
        List<ReviewItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {}
