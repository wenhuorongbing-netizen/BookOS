package com.bookos.backend.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateUserBookRatingRequest(@NotNull @Min(0) @Max(5) Integer rating) {}
