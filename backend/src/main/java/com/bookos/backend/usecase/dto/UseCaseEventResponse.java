package com.bookos.backend.usecase.dto;

import com.bookos.backend.usecase.entity.UseCaseEventType;
import java.time.Instant;

public record UseCaseEventResponse(
        Long id,
        UseCaseEventType eventType,
        String contextType,
        String contextId,
        Instant createdAt) {
}
