package com.bookos.backend.quote.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuoteRequest(
        @NotNull(message = "Book id is required.")
        Long bookId,

        @NotBlank(message = "Quote text is required.")
        @Size(max = 10000, message = "Quote text must be at most 10000 characters.")
        String text,

        @Size(max = 220, message = "Attribution must be at most 220 characters.")
        String attribution,

        Long sourceReferenceId,

        @Min(value = 1, message = "Page start must be positive.")
        Integer pageStart,

        @Min(value = 1, message = "Page end must be positive.")
        Integer pageEnd,

        List<@Size(max = 80, message = "Tag must be at most 80 characters.") String> tags,

        List<@Size(max = 120, message = "Concept must be at most 120 characters.") String> concepts,

        Visibility visibility) {}
