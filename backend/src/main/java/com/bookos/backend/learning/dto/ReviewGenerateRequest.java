package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewGenerateRequest(
        @NotNull Long id,
        @Size(max = 220) String title,
        @Size(max = 64) String mode,
        @Min(1) Integer limit) {}
