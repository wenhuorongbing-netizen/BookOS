package com.bookos.backend.daily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DailyReflectionRequest(
        @NotNull(message = "Daily target is required.")
        DailyTarget target,

        Long dailySentenceId,

        Long dailyDesignPromptId,

        @NotBlank(message = "Reflection text is required.")
        @Size(max = 10000, message = "Reflection must be at most 10000 characters.")
        String reflectionText) {}
