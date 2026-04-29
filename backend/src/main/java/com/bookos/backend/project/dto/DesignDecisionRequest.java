package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DesignDecisionRequest(
        @NotBlank(message = "Decision title is required.")
        @Size(max = 220, message = "Decision title must be at most 220 characters.")
        String title,

        @NotBlank(message = "Decision is required.")
        @Size(max = 10000, message = "Decision must be at most 10000 characters.")
        String decision,

        @Size(max = 10000, message = "Rationale must be at most 10000 characters.")
        String rationale,

        @Size(max = 10000, message = "Tradeoffs must be at most 10000 characters.")
        String tradeoffs,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status) {}
