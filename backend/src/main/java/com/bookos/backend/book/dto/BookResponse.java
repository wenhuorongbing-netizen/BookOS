package com.bookos.backend.book.dto;

import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.common.enums.Visibility;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String subtitle,
        String description,
        String isbn,
        String publisher,
        Integer publicationYear,
        String coverUrl,
        String category,
        Visibility visibility,
        List<String> authors,
        List<String> tags,
        boolean inLibrary,
        Long userBookId,
        ReadingStatus readingStatus,
        ReadingFormat readingFormat,
        OwnershipStatus ownershipStatus,
        Integer progressPercent,
        Integer rating) {}
