package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ProjectKnowledgeLinkResponse(
        Long id,
        Long projectId,
        String targetType,
        Long targetId,
        String relationshipType,
        String note,
        SourceReferenceResponse sourceReference,
        Instant createdAt) {}
