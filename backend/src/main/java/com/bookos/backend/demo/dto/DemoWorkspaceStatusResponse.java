package com.bookos.backend.demo.dto;

import java.util.List;
import java.util.Map;

public record DemoWorkspaceStatusResponse(
        boolean active,
        Long bookId,
        Long projectId,
        Long quoteId,
        Long actionItemId,
        Long forumThreadId,
        List<Long> conceptIds,
        Map<String, Long> recordCounts,
        String label,
        String safetyNote) {}
