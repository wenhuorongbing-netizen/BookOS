package com.bookos.backend.admin.dto;

import java.util.List;

public record OntologyImportResponse(
        boolean dryRun,
        int booksCreated,
        int booksExisting,
        int conceptsCreated,
        int conceptsUpdated,
        int conceptsExisting,
        int knowledgeObjectsCreated,
        int knowledgeObjectsUpdated,
        int knowledgeObjectsExisting,
        int sourceReferencesCreated,
        List<String> warnings) {}
