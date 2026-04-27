package com.bookos.backend.action.dto;

import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActionItemRequest(
        @NotNull(message = "Book id is required.")
        Long bookId,

        @NotBlank(message = "Action item title is required.")
        @Size(max = 220, message = "Action item title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        ActionPriority priority,

        Long sourceReferenceId,

        @Min(value = 1, message = "Page start must be at least 1.")
        Integer pageStart,

        @Min(value = 1, message = "Page end must be at least 1.")
        Integer pageEnd,

        Visibility visibility) {}
