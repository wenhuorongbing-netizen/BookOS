package com.bookos.backend.book.dto;

import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;
import java.util.List;

public record UserBookResponse(
        Long id,
        Long bookId,
        String title,
        String subtitle,
        String coverUrl,
        String category,
        List<String> authors,
        List<String> tags,
        ReadingStatus readingStatus,
        ReadingFormat readingFormat,
        OwnershipStatus ownershipStatus,
        Integer progressPercent,
        Integer rating) {}
