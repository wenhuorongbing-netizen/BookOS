package com.bookos.backend.admin.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OntologySeedBookRequest(
        @NotBlank(message = "Book title is required.")
        @Size(max = 220, message = "Book title must be at most 220 characters.")
        String title,

        @Size(max = 220, message = "Subtitle must be at most 220 characters.")
        String subtitle,

        @Size(max = 3000, message = "Summary must be at most 3000 characters.")
        String summary,

        @Size(max = 180, message = "Publisher must be at most 180 characters.")
        String publisher,

        Integer publicationYear,

        @Size(max = 120, message = "Category must be at most 120 characters.")
        String category,

        Visibility visibility,

        List<@Size(max = 180, message = "Author must be at most 180 characters.") String> authors,

        List<@Size(max = 120, message = "Tag must be at most 120 characters.") String> tags) {}
