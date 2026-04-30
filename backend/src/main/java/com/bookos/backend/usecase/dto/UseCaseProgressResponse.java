package com.bookos.backend.usecase.dto;

import com.bookos.backend.usecase.entity.UseCaseProgressStatus;
import java.time.Instant;
import java.util.Set;

public record UseCaseProgressResponse(
        String useCaseSlug,
        UseCaseProgressStatus status,
        Integer currentStep,
        Set<String> completedStepKeys,
        Set<String> automaticCompletedStepKeys,
        Set<String> effectiveCompletedStepKeys,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt) {}
