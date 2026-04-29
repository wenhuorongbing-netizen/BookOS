package com.bookos.backend.project.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GameProjectRequest(
        @NotBlank(message = "Project title is required.")
        @Size(max = 220, message = "Project title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        @Size(max = 120, message = "Genre must be at most 120 characters.")
        String genre,

        @Size(max = 120, message = "Platform must be at most 120 characters.")
        String platform,

        @Size(max = 64, message = "Stage must be at most 64 characters.")
        String stage,

        Visibility visibility,

        @Min(value = 0, message = "Progress must be at least 0.")
        @Max(value = 100, message = "Progress must be at most 100.")
        Integer progressPercent) {}
