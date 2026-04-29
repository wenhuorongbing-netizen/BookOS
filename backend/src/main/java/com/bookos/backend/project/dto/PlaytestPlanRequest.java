package com.bookos.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaytestPlanRequest(
        @NotBlank(message = "Playtest plan title is required.")
        @Size(max = 220, message = "Playtest plan title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Hypothesis must be at most 10000 characters.")
        String hypothesis,

        @Size(max = 10000, message = "Target players must be at most 10000 characters.")
        String targetPlayers,

        @Size(max = 10000, message = "Tasks must be at most 10000 characters.")
        String tasks,

        @Size(max = 10000, message = "Success criteria must be at most 10000 characters.")
        String successCriteria,

        @Size(max = 64, message = "Status must be at most 64 characters.")
        String status) {}
