package com.bookos.backend.usecase.dto;

import com.bookos.backend.usecase.entity.UseCaseProgressStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record UseCaseProgressResponse(
        String useCaseSlug,
        UseCaseProgressStatus status,
        Integer currentStep,
        Set<String> completedStepKeys,
        Set<String> automaticCompletedStepKeys,
        Set<String> effectiveCompletedStepKeys,
        Map<String, UseCaseStepVerificationResponse> stepVerification,
        Instant startedAt,
        Instant completedAt,
        Instant updatedAt) {}
