package com.bookos.backend.learning.dto;

public record BookAnalyticsResponse(
        Long bookId,
        String bookTitle,
        long notesCount,
        long capturesCount,
        long quotesCount,
        long actionItemsCount,
        long completedActionItemsCount,
        long conceptsCount,
        long readingSessionsCount,
        long totalMinutesRead,
        long reviewItemsCount) {}
