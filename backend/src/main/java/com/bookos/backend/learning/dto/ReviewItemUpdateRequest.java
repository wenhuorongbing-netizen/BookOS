package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewItemUpdateRequest(
        @Size(max = 10000) String userResponse,
        @Size(max = 64) String status,
        @Min(0) @Max(5) Integer confidenceScore) {}
