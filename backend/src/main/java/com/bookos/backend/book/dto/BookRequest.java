package com.bookos.backend.book.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BookRequest(
        @NotBlank @Size(max = 220) String title,
        @Size(max = 220) String subtitle,
        @Size(max = 3000) String description,
        @Size(max = 50) String isbn,
        @Size(max = 180) String publisher,
        Integer publicationYear,
        @Size(max = 500) String coverUrl,
        @Size(max = 120) String category,
        Visibility visibility,
        List<@NotBlank @Size(max = 180) String> authors,
        List<@NotBlank @Size(max = 120) String> tags) {}
