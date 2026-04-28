package com.bookos.backend.note.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookNoteRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 220, message = "Title must be at most 220 characters.")
        String title,

        @NotBlank(message = "Markdown content is required.")
        @Size(max = 50000, message = "Markdown content must be at most 50000 characters.")
        String markdown,

        Visibility visibility,

        @Size(max = 1000, message = "Three-sentence summary must be at most 1000 characters.")
        String threeSentenceSummary) {}
