package com.bookos.backend.ai.dto;

public record AIProviderStatusResponse(
        boolean enabled,
        boolean available,
        String configuredProvider,
        String activeProvider,
        boolean externalProviderConfigured,
        int maxInputChars,
        String message) {}
