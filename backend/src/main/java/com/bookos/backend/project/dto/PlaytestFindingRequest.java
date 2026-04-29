package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PlaytestFindingRequest(
        @Positive(message = "Playtest session id must be positive.")
        Long sessionId,

        @NotBlank(message = "Finding title is required.")
        @Size(max = 220, message = "Finding title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Observation must be at most 10000 characters.")
        String observation,

        @Size(max = 64, message = "Severity must be at most 64 characters.")
        String severity,

        @Size(max = 10000, message = "Recommendation must be at most 10000 characters.")
        String recommendation,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status) {}
