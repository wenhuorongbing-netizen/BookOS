package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReadingSessionStartRequest(
        @NotNull Long bookId,
        @Min(0) Integer startPage,
        @Size(max = 10000) String reflection) {}
