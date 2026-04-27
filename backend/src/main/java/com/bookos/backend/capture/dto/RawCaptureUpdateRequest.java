package com.bookos.backend.capture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RawCaptureUpdateRequest(
        @NotBlank @Size(max = 5000) String rawText) {}
