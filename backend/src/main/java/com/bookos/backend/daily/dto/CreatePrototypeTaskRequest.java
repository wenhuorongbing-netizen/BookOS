package com.bookos.backend.daily.dto;

import jakarta.validation.constraints.Size;

public record CreatePrototypeTaskRequest(
        Long dailyDesignPromptId,

        @Size(max = 220, message = "Title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description) {}
