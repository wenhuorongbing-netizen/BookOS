package com.bookos.backend.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForumReportRequest(
        @NotBlank(message = "Report reason is required.")
        @Size(max = 120, message = "Report reason must be at most 120 characters.")
        String reason,

        @Size(max = 5000, message = "Report details must be at most 5000 characters.")
        String details) {}
