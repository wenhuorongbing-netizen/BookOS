package com.bookos.backend.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ForumCommentRequest(
        @NotBlank(message = "Comment body is required.")
        @Size(max = 12000, message = "Comment body must be at most 12000 characters.")
        String bodyMarkdown,

        @Positive(message = "Parent comment id must be positive.")
        Long parentCommentId) {}
