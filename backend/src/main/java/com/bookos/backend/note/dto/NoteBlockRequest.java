package com.bookos.backend.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteBlockRequest(
        @NotBlank(message = "Raw text is required.")
        @Size(max = 5000, message = "Raw text must be at most 5000 characters.")
        String rawText,
        Integer sortOrder) {}
