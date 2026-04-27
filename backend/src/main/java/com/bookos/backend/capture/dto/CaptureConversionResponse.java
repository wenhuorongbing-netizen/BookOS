package com.bookos.backend.capture.dto;

public record CaptureConversionResponse(
        RawCaptureResponse capture,
        String targetType,
        Long targetId) {}
