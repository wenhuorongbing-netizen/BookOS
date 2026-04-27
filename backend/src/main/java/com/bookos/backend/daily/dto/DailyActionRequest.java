package com.bookos.backend.daily.dto;

import jakarta.validation.constraints.NotNull;

public record DailyActionRequest(
        @NotNull(message = "Daily target is required.")
        DailyTarget target) {}
