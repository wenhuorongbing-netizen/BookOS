package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectApplicationRequest(
        @Size(max = 64, message = "Source entity type must be at most 64 characters.")
        String sourceEntityType,

        @Positive(message = "Source entity id must be positive.")
        Long sourceEntityId,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Size(max = 64, message = "Application type must be at most 64 characters.")
        String applicationType,

        @NotBlank(message = "Application title is required.")
        @Size(max = 220, message = "Application title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status) {}
