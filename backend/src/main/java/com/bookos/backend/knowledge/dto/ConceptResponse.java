package com.bookos.backend.knowledge.dto;

import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.util.List;

public record ConceptResponse(
        Long id,
        String name,
        String slug,
        String description,
        Visibility visibility,
        String ontologyLayer,
        SourceConfidence sourceConfidence,
        String createdBy,
        List<String> tags,
        Long bookId,
        String bookTitle,
        Integer mentionCount,
        SourceReferenceResponse firstSourceReference,
        List<SourceReferenceResponse> sourceReferences,
        Instant createdAt,
        Instant updatedAt) {}
