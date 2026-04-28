package com.bookos.backend.ai.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AISuggestionRequest(
        @Positive(message = "Book id must be positive.")
        Long bookId,

        @Positive(message = "Note id must be positive.")
        Long noteId,

        @Positive(message = "Raw capture id must be positive.")
        Long rawCaptureId,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Size(max = 20000, message = "AI source text must be at most 20000 characters.")
        String text) {}
