package com.bookos.backend.graph.dto;

import com.bookos.backend.common.enums.SourceConfidence;
import java.time.Instant;

public record GraphEdgeResponse(
        String source,
        String target,
        String type,
        Long entityLinkId,
        Long sourceReferenceId,
        SourceConfidence sourceConfidence,
        String createdBy,
        boolean systemCreated,
        String note,
        Instant createdAt) {}
