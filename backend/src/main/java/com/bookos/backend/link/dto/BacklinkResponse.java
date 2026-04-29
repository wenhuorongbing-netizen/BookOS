package com.bookos.backend.link.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record BacklinkResponse(
        Long linkId,
        String direction,
        String relationType,
        String entityType,
        Long entityId,
        String title,
        String excerpt,
        SourceReferenceResponse sourceReference,
        Instant createdAt) {}
