package com.bookos.backend.user.dto;

import com.bookos.backend.common.enums.RoleName;

public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        String bio,
        RoleName role,
        boolean onboardingCompleted,
        String primaryUseCase,
        String startingMode,
        String preferredDashboardMode) {}
