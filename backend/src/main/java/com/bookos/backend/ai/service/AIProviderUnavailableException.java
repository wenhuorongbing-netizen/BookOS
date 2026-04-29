package com.bookos.backend.ai.service;

public class AIProviderUnavailableException extends RuntimeException {

    public AIProviderUnavailableException(String message) {
        super(message);
    }
}
