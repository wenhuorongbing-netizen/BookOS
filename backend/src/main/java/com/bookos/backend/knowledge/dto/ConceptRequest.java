package com.bookos.backend.knowledge.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ConceptRequest(
        @NotBlank(message = "Concept name is required.")
        @Size(max = 180, message = "Concept name must be at most 180 characters.")
        String name,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        Visibility visibility,

        @Positive(message = "Book id must be positive.")
        Long bookId,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId) {}
