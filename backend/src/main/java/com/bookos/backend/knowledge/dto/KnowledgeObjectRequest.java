package com.bookos.backend.knowledge.dto;

import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record KnowledgeObjectRequest(
        @NotNull(message = "Knowledge object type is required.")
        KnowledgeObjectType type,

        @NotBlank(message = "Title is required.")
        @Size(max = 220, message = "Title must be at most 220 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        Visibility visibility,

        Long bookId,

        Long noteId,

        Long conceptId,

        Long sourceReferenceId,

        List<@Size(max = 80, message = "Tag must be at most 80 characters.") String> tags) {}
