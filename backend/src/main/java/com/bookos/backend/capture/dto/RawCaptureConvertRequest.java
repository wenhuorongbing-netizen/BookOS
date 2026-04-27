package com.bookos.backend.capture.dto;

import jakarta.validation.constraints.Size;

public record RawCaptureConvertRequest(
        CaptureConversionTarget targetType,
        @Size(max = 220) String title) {}
