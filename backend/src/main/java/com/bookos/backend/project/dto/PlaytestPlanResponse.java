package com.bookos.backend.project.dto;

import java.time.Instant;

public record PlaytestPlanResponse(
        Long id,
        Long projectId,
        String title,
        String hypothesis,
        String targetPlayers,
        String tasks,
        String successCriteria,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
