package com.bookos.backend.ai.dto;

public record AISuggestionRequest(
        Long bookId,
        Long noteId,
        Long rawCaptureId,
        Long sourceReferenceId,
        String text) {}
