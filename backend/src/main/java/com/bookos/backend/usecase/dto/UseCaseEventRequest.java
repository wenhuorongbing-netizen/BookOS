package com.bookos.backend.usecase.dto;

import com.bookos.backend.usecase.entity.UseCaseEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UseCaseEventRequest(
        @NotNull UseCaseEventType eventType,
        @Size(max = 80) String contextType,
        @Size(max = 120) String contextId) {
}
