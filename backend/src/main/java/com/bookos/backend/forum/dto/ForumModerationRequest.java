package com.bookos.backend.forum.dto;

import com.bookos.backend.common.enums.ForumThreadStatus;
import jakarta.validation.constraints.NotNull;

public record ForumModerationRequest(
        @NotNull(message = "Thread status is required.")
        ForumThreadStatus status) {}
