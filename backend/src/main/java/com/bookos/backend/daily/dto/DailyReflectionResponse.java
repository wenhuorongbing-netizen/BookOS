package com.bookos.backend.daily.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DailyReflectionResponse(
        Long id,
        LocalDate day,
        DailyTarget target,
        Long dailySentenceId,
        Long dailyDesignPromptId,
        String reflectionText,
        Instant createdAt,
        Instant updatedAt) {}
