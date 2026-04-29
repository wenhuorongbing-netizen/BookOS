package com.bookos.backend.ai.service;

import java.util.List;

public record AIDraft(
        String draftText,
        String draftJson,
        List<String> validationWarnings) {

    public AIDraft(String draftText, String draftJson) {
        this(draftText, draftJson, List.of());
    }
}
