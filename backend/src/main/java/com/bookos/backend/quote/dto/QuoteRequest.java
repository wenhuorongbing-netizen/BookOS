package com.bookos.backend.quote.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuoteRequest(
        @NotNull(message = "Book id is required.")
        Long bookId,

        @NotBlank(message = "Quote text is required.")
        @Size(max = 10000, message = "Quote text must be at most 10000 characters.")
        String text,

        @Size(max = 220, message = "Attribution must be at most 220 characters.")
        String attribution,

        Long sourceReferenceId,

        Visibility visibility) {}
