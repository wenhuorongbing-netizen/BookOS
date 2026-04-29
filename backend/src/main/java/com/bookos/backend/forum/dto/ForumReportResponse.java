package com.bookos.backend.forum.dto;

import com.bookos.backend.common.enums.ForumReportStatus;
import java.time.Instant;

public record ForumReportResponse(
        Long id,
        Long threadId,
        String threadTitle,
        Long reporterId,
        String reporterDisplayName,
        String reason,
        String details,
        ForumReportStatus status,
        boolean resolved,
        Instant createdAt,
        Instant updatedAt) {}
