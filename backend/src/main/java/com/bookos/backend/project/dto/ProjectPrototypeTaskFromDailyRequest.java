package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectPrototypeTaskFromDailyRequest(
        @NotNull(message = "Daily design prompt id is required.")
        @Positive(message = "Daily design prompt id must be positive.")
        Long dailyDesignPromptId,

        @Size(max = 220, message = "Title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description) {}
