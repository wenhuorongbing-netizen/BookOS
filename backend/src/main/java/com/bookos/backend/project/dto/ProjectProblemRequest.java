package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectProblemRequest(
        @NotBlank(message = "Problem title is required.")
        @Size(max = 220, message = "Problem title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status,

        @Size(max = 64, message = "Priority must be at most 64 characters.")
        String priority,

        @Positive(message = "Source reference id must be positive.")
        Long relatedSourceReferenceId) {}
