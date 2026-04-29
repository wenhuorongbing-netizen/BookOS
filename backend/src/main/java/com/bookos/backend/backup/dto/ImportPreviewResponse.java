package com.bookos.backend.backup.dto;

import java.util.List;

public record ImportPreviewResponse(
        String importType,
        int totalRecords,
        int recordsToCreate,
        int potentialDuplicates,
        List<ImportRecordPreview> records,
        List<String> warnings,
        List<String> unsupportedFields,
        List<String> sourceReferenceIssues,
        List<String> pageNumberIssues) {}
