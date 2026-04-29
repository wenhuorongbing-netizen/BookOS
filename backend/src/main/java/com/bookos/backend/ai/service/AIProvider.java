package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;

public interface AIProvider {

    String providerName();

    default boolean available() {
        return true;
    }

    AIDraft generate(AISuggestionType type, String sourceText, String sourceTitle);
}
