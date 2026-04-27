package com.bookos.backend.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateUserBookProgressRequest(@NotNull @Min(0) @Max(100) Integer progressPercent) {}
