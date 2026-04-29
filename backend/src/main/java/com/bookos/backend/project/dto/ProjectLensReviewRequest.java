package com.bookos.backend.project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectLensReviewRequest(
        @Positive(message = "Knowledge object id must be positive.")
        Long knowledgeObjectId,

        @NotBlank(message = "Lens review question is required.")
        @Size(max = 500, message = "Question must be at most 500 characters.")
        String question,

        @Size(max = 10000, message = "Answer must be at most 10000 characters.")
        String answer,

        @Min(value = 0, message = "Score must be at least 0.")
        @Max(value = 10, message = "Score must be at most 10.")
        Integer score,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId) {}
