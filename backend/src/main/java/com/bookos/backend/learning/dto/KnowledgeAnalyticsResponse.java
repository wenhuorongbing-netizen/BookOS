package com.bookos.backend.learning.dto;

import java.util.List;

public record KnowledgeAnalyticsResponse(
        long conceptsCount,
        long knowledgeObjectsCount,
        long masteryTargets,
        long dueForReview,
        long reviewItems,
        long completedReviewItems,
        List<AnalyticsCountResponse> mostLinkedConcepts) {}
