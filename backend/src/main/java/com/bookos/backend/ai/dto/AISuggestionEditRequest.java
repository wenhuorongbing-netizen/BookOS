package com.bookos.backend.ai.dto;

import jakarta.validation.constraints.Size;

public record AISuggestionEditRequest(
        @Size(max = 10000, message = "Draft text must be 10000 characters or less.") String draftText,

        @Size(max = 20000, message = "Draft JSON must be 20000 characters or less.")
        String draftJson) {}
