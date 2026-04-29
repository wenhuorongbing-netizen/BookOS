package com.bookos.backend.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EntityLinkRequest(
        @NotBlank(message = "Source type is required.")
        @Size(max = 64, message = "Source type must be at most 64 characters.")
        String sourceType,

        @NotNull(message = "Source id is required.")
        @Positive(message = "Source id must be positive.")
        Long sourceId,

        @NotBlank(message = "Target type is required.")
        @Size(max = 64, message = "Target type must be at most 64 characters.")
        String targetType,

        @NotNull(message = "Target id is required.")
        @Positive(message = "Target id must be positive.")
        Long targetId,

        @NotBlank(message = "Relation type is required.")
        @Size(max = 80, message = "Relation type must be at most 80 characters.")
        String relationType,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Size(max = 2000, message = "Relationship note must be at most 2000 characters.")
        String note) {}
