package com.bookos.backend.user.dto;

public record AuthResponse(
        String token,
        CurrentUserResponse user) {}
