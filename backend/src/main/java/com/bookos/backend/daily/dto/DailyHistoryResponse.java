package com.bookos.backend.daily.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DailyHistoryResponse(
        Long id,
        LocalDate day,
        DailyTarget target,
        String action,
        String sourceType,
        Long sourceId,
        Long dailySentenceId,
        Long dailyDesignPromptId,
        Instant createdAt) {}
