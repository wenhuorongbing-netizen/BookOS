package com.bookos.backend.graph.dto;

import com.bookos.backend.common.enums.SourceConfidence;
import java.time.Instant;

public record GraphNodeResponse(
        String id,
        String type,
        String label,
        Long entityId,
        Long sourceReferenceId,
        SourceConfidence sourceConfidence,
        Instant createdAt) {}
