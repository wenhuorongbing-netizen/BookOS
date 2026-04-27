package com.bookos.backend.book.dto;

import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;

public record AddToLibraryRequest(
        ReadingStatus readingStatus,
        ReadingFormat readingFormat,
        OwnershipStatus ownershipStatus) {}
