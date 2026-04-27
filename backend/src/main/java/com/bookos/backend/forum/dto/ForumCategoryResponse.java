package com.bookos.backend.forum.dto;

import java.time.Instant;

public record ForumCategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        long threadCount,
        Instant createdAt,
        Instant updatedAt) {}
