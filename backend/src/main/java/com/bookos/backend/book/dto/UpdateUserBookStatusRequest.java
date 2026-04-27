package com.bookos.backend.book.dto;

import com.bookos.backend.common.enums.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserBookStatusRequest(@NotNull ReadingStatus status) {}
