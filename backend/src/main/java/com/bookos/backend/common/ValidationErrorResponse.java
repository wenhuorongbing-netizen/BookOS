package com.bookos.backend.common;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        boolean success,
        String message,
        Map<String, String> errors,
        Instant timestamp) {

    public static ValidationErrorResponse of(String message, Map<String, String> errors) {
        return new ValidationErrorResponse(false, message, errors, Instant.now());
    }
}
