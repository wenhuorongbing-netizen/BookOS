package com.bookos.backend.user.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.user.dto.UserAdminResponse;
import com.bookos.backend.user.dto.UserProfileResponse;
import com.bookos.backend.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me/profile")
    public ApiResponse<UserProfileResponse> profile(Authentication authentication) {
        return ApiResponse.ok("Profile loaded.", userService.currentProfile(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserAdminResponse>> listUsers() {
        return ApiResponse.ok("Users loaded.", userService.listUsers());
    }
}
