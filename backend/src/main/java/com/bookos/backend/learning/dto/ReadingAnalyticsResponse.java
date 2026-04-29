package com.bookos.backend.learning.dto;

import java.util.List;

public record ReadingAnalyticsResponse(
        long libraryBooks,
        long currentlyReadingBooks,
        long completedBooks,
        long notesCount,
        long capturesCount,
        long quotesCount,
        long openActionItems,
        long completedActionItems,
        long conceptsCount,
        long dailyReflectionsCount,
        long projectApplicationsCount,
        long reviewSessionsCount,
        long completedReviewSessions,
        long totalMinutesRead,
        List<AnalyticsCountResponse> mostActiveBooks) {}
