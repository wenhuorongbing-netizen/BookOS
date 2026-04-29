package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Primary
@Component
@RequiredArgsConstructor
public class AIProviderRouter implements AIProvider {

    private static final String MOCK = "mock";
    private static final String OPENAI_COMPATIBLE = "openai-compatible";

    private final AIProviderProperties properties;
    private final MockAIProvider mockAIProvider;
    private final OpenAICompatibleProvider openAICompatibleProvider;

    @Override
    public String providerName() {
        return activeProvider().providerName();
    }

    @Override
    public boolean available() {
        return status().available();
    }

    @Override
    public AIDraft generate(AISuggestionType type, String sourceText, String sourceTitle) {
        AIProviderStatus status = status();
        if (!status.available()) {
            throw new AIProviderUnavailableException(status.message());
        }
        return activeProvider().generate(type, sourceText, sourceTitle);
    }

    public AIProviderStatus status() {
        String configuredProvider = configuredProvider();
        boolean externalConfigured = StringUtils.hasText(properties.getOpenaiCompatible().getApiKey())
                && StringUtils.hasText(properties.getOpenaiCompatible().getBaseUrl())
                && StringUtils.hasText(properties.getOpenaiCompatible().getModel());

        if (!properties.isEnabled()) {
            return new AIProviderStatus(
                    false,
                    false,
                    configuredProvider,
                    "disabled",
                    externalConfigured,
                    properties.getMaxInputChars(),
                    "AI suggestions are disabled by AI_ENABLED=false.");
        }

        if (OPENAI_COMPATIBLE.equals(configuredProvider)) {
            if (!externalConfigured) {
                return new AIProviderStatus(
                        true,
                        false,
                        configuredProvider,
                        openAICompatibleProvider.providerName(),
                        false,
                        properties.getMaxInputChars(),
                        "OpenAI-compatible provider is selected but base URL, model, or API key is missing.");
            }
            return new AIProviderStatus(
                    true,
                    true,
                    configuredProvider,
                    openAICompatibleProvider.providerName(),
                    true,
                    properties.getMaxInputChars(),
                    "OpenAI-compatible provider is configured. Suggestions remain draft-only.");
        }

        return new AIProviderStatus(
                true,
                true,
                configuredProvider,
                mockAIProvider.providerName(),
                externalConfigured,
                properties.getMaxInputChars(),
                "MockAIProvider is active. No external AI calls are made.");
    }

    private AIProvider activeProvider() {
        return OPENAI_COMPATIBLE.equals(configuredProvider()) ? openAICompatibleProvider : mockAIProvider;
    }

    private String configuredProvider() {
        String provider = properties.getProvider();
        if (!StringUtils.hasText(provider)) {
            return MOCK;
        }
        String normalized = provider.trim().toLowerCase(java.util.Locale.ROOT);
        return OPENAI_COMPATIBLE.equals(normalized) ? OPENAI_COMPATIBLE : MOCK;
    }
}
