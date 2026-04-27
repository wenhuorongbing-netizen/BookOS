package com.bookos.backend.capture.dto;

import com.bookos.backend.knowledge.dto.ConceptResponse;
import com.bookos.backend.knowledge.dto.KnowledgeObjectResponse;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.util.List;

public record ReviewedConceptResponse(
        String rawName,
        String finalName,
        ConceptReviewAction action,
        ConceptResponse concept,
        KnowledgeObjectResponse knowledgeObject,
        SourceReferenceResponse sourceReference,
        List<String> tags) {}
