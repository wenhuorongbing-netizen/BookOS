package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record KnowledgeMasteryRequest(
        @NotBlank @Size(max = 64) String targetType,
        @NotNull Long targetId,
        @Min(0) @Max(5) Integer familiarityScore,
        @Min(0) @Max(5) Integer usefulnessScore,
        Instant nextReviewAt,
        Long sourceReferenceId) {}
