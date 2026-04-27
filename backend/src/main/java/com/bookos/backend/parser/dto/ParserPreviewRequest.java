package com.bookos.backend.parser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParserPreviewRequest(
        @NotBlank(message = "Raw text is required.")
        @Size(max = 5000, message = "Raw text must be at most 5000 characters.")
        String rawText) {}
