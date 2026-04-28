package com.bookos.backend.capture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ConceptReviewItemRequest(
        @NotBlank(message = "Raw concept name is required.")
        @Size(max = 180, message = "Raw concept name must be at most 180 characters.")
        String rawName,

        @Size(max = 180, message = "Final concept name must be at most 180 characters.")
        String finalName,

        @NotNull(message = "Concept review action is required.")
        ConceptReviewAction action,

        @Positive(message = "Existing concept id must be positive.")
        Long existingConceptId,

        List<@Size(max = 80, message = "Tag must be at most 80 characters.") String> tags) {}
