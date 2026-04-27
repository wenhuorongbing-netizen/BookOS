package com.bookos.backend.forum.dto;

public record StructuredPostTemplateResponse(
        Long id,
        String name,
        String slug,
        String description,
        String bodyMarkdownTemplate,
        String defaultRelatedEntityType,
        Integer sortOrder) {}
