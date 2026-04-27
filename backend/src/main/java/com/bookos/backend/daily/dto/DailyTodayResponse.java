package com.bookos.backend.daily.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyTodayResponse(
        LocalDate day,
        DailySentenceResponse sentence,
        DailyDesignPromptResponse prompt,
        List<DailyReflectionResponse> reflections) {}
