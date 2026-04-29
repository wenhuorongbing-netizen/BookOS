package com.bookos.backend.project.dto;

import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ProjectLensReviewResponse(
        Long id,
        Long projectId,
        Long knowledgeObjectId,
        String knowledgeObjectTitle,
        String question,
        String answer,
        Integer score,
        String status,
        SourceReferenceResponse sourceReference,
        Instant createdAt,
        Instant updatedAt) {}
