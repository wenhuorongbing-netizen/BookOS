package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;

public interface AIProvider {

    String providerName();

    MockAIDraft generate(AISuggestionType type, String sourceText, String sourceTitle);
}
