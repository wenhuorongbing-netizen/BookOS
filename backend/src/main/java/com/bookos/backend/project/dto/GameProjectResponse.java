package com.bookos.backend.project.dto;

import com.bookos.backend.common.enums.Visibility;
import java.time.Instant;

public record GameProjectResponse(
        Long id,
        String title,
        String slug,
        String description,
        String genre,
        String platform,
        String stage,
        Visibility visibility,
        Integer progressPercent,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt) {}
