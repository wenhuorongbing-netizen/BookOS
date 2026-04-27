package com.bookos.backend.forum.dto;

import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ForumThreadRequest(
        @NotNull(message = "Category is required.")
        Long categoryId,

        @NotBlank(message = "Thread title is required.")
        @Size(max = 220, message = "Thread title must be at most 220 characters.")
        String title,

        @NotBlank(message = "Thread body is required.")
        @Size(max = 20000, message = "Thread body must be at most 20000 characters.")
        String bodyMarkdown,

        @Size(max = 64, message = "Related entity type must be at most 64 characters.")
        String relatedEntityType,

        Long relatedEntityId,

        Long relatedBookId,

        Long relatedConceptId,

        Long sourceReferenceId,

        Visibility visibility) {}
