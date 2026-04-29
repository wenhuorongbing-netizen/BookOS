package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectKnowledgeLinkRequest(
        @NotBlank(message = "Target type is required.")
        @Size(max = 64, message = "Target type must be at most 64 characters.")
        String targetType,

        @NotNull(message = "Target id is required.")
        @Positive(message = "Target id must be positive.")
        Long targetId,

        @Size(max = 64, message = "Relationship type must be at most 64 characters.")
        String relationshipType,

        @Size(max = 10000, message = "Note must be at most 10000 characters.")
        String note,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId) {}
