package com.bookos.backend.user.dto;

import jakarta.validation.constraints.Size;

public record OnboardingPreferenceRequest(
        Boolean onboardingCompleted,
        @Size(max = 80) String primaryUseCase,
        @Size(max = 40) String startingMode,
        @Size(max = 40) String preferredDashboardMode) {}
