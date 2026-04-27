package com.bookos.backend.ai.dto;

import jakarta.validation.constraints.Size;

public record AISuggestionEditRequest(
        @Size(max = 10000, message = "Draft text must be 10000 characters or less.") String draftText,
        String draftJson) {}
