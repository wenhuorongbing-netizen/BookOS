package com.bookos.backend.ai.service;

public record AIProviderStatus(
        boolean enabled,
        boolean available,
        String configuredProvider,
        String activeProvider,
        boolean externalProviderConfigured,
        int maxInputChars,
        String message) {}
