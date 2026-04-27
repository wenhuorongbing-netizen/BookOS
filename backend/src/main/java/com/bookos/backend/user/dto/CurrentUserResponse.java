package com.bookos.backend.user.dto;

import com.bookos.backend.common.enums.RoleName;

public record CurrentUserResponse(
        Long id,
        String email,
        String displayName,
        RoleName role) {}
