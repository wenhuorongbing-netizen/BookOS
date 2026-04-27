package com.bookos.backend.forum.dto;

import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;

public record ForumThreadResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categorySlug,
        Long authorId,
        String authorDisplayName,
        String title,
        String bodyMarkdown,
        String relatedEntityType,
        Long relatedEntityId,
        Long relatedBookId,
        String relatedBookTitle,
        Long relatedConceptId,
        String relatedConceptName,
        Long sourceReferenceId,
        SourceReferenceResponse sourceReference,
        ForumThreadStatus status,
        Visibility visibility,
        long commentCount,
        long likeCount,
        long bookmarkCount,
        boolean likedByCurrentUser,
        boolean bookmarkedByCurrentUser,
        boolean canEdit,
        Instant createdAt,
        Instant updatedAt) {}
