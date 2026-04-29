package com.bookos.backend.backup.dto;

import java.util.List;

public record ImportCommitResponse(
        int booksCreated,
        int notesCreated,
        int capturesCreated,
        int quotesCreated,
        int actionItemsCreated,
        int conceptsCreated,
        int knowledgeObjectsCreated,
        int sourceReferencesCreated,
        int projectsCreated,
        int duplicatesSkipped,
        List<String> warnings) {}
