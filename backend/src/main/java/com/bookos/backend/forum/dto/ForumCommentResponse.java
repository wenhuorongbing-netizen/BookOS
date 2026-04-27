package com.bookos.backend.forum.dto;

import java.time.Instant;

public record ForumCommentResponse(
        Long id,
        Long threadId,
        Long authorId,
        String authorDisplayName,
        String bodyMarkdown,
        Long parentCommentId,
        boolean canEdit,
        Instant createdAt,
        Instant updatedAt) {}
