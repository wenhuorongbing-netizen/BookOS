package com.bookos.backend.knowledge.dto;

import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.util.List;

public record KnowledgeObjectResponse(
        Long id,
        KnowledgeObjectType type,
        String title,
        String slug,
        String description,
        Visibility visibility,
        String ontologyLayer,
        SourceConfidence sourceConfidence,
        String createdBy,
        Long bookId,
        String bookTitle,
        Long noteId,
        String noteTitle,
        Long conceptId,
        String conceptName,
        SourceReferenceResponse sourceReference,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt) {}
