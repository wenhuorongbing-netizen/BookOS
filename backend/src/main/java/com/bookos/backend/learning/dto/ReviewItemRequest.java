package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewItemRequest(
        @NotBlank @Size(max = 64) String targetType,
        @NotNull Long targetId,
        Long sourceReferenceId,
        @NotBlank @Size(max = 10000) String prompt) {}
