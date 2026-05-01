package com.bookos.backend.usecase.dto;

public record UseCaseStepVerificationResponse(
        String stepKey,
        String state,
        boolean complete,
        boolean automatic,
        boolean manual,
        String message) {}
