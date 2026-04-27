package com.bookos.backend.link.dto;

import java.time.Instant;

public record EntityLinkResponse(
        Long id,
        String sourceType,
        Long sourceId,
        String targetType,
        Long targetId,
        String relationType,
        Long sourceReferenceId,
        Instant createdAt) {}
