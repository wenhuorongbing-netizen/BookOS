package com.bookos.backend.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EntityLinkRequest(
        @NotBlank(message = "Source type is required.")
        @Size(max = 64, message = "Source type must be at most 64 characters.")
        String sourceType,

        @NotNull(message = "Source id is required.")
        Long sourceId,

        @NotBlank(message = "Target type is required.")
        @Size(max = 64, message = "Target type must be at most 64 characters.")
        String targetType,

        @NotNull(message = "Target id is required.")
        Long targetId,

        @NotBlank(message = "Relation type is required.")
        @Size(max = 80, message = "Relation type must be at most 80 characters.")
        String relationType,

        Long sourceReferenceId) {}
