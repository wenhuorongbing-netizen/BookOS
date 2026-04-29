package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ApplySourceRequest(
        @NotNull(message = "Source id is required.")
        @Positive(message = "Source id must be positive.")
        Long sourceId,

        @Size(max = 220, message = "Title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        @Size(max = 64, message = "Application type must be at most 64 characters.")
        String applicationType) {}
