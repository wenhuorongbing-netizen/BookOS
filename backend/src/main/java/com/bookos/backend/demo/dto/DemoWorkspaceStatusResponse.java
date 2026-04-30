package com.bookos.backend.demo.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DemoWorkspaceStatusResponse(
        boolean active,
        Instant lastResetAt,
        Long bookId,
        Long projectId,
        Long quoteId,
        Long actionItemId,
        Long forumThreadId,
        List<Long> conceptIds,
        Map<String, Long> recordCounts,
        List<String> includedRecordTypes,
        boolean excludedFromAnalyticsByDefault,
        String label,
        String safetyNote) {}
