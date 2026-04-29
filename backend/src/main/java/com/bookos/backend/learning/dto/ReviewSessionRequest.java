package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewSessionRequest(
        @NotBlank @Size(max = 220) String title,
        @Size(max = 64) String mode,
        @Size(max = 64) String scopeType,
        Long scopeId,
        @Size(max = 10000) String summary) {}
