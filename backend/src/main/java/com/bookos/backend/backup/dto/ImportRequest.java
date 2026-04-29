package com.bookos.backend.backup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImportRequest(
        @NotBlank(message = "Import type is required.")
        @Size(max = 40, message = "Import type must be at most 40 characters.")
        String importType,

        @NotBlank(message = "Import content is required.")
        @Size(max = 1_000_000, message = "Import payload must be at most 1 MB.")
        String content,

        @Size(max = 220, message = "Book title must be at most 220 characters.")
        String bookTitle,

        @Size(max = 120, message = "File name must be at most 120 characters.")
        String fileName) {}
