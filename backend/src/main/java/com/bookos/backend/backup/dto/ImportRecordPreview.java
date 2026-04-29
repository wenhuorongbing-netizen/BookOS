package com.bookos.backend.backup.dto;

public record ImportRecordPreview(
        String type,
        String title,
        String stableKey,
        boolean duplicate,
        String action,
        Long existingId) {}
