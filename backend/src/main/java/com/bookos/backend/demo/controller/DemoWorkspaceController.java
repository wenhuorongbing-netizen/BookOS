package com.bookos.backend.demo.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.demo.dto.DemoWorkspaceStatusResponse;
import com.bookos.backend.demo.service.DemoWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class DemoWorkspaceController {

    private final DemoWorkspaceService demoWorkspaceService;

    @GetMapping("/status")
    public ApiResponse<DemoWorkspaceStatusResponse> status(Authentication authentication) {
        return ApiResponse.ok("Demo workspace status loaded.", demoWorkspaceService.status(authentication.getName()));
    }

    @PostMapping("/start")
    public ApiResponse<DemoWorkspaceStatusResponse> start(Authentication authentication) {
        return ApiResponse.ok("Demo workspace started.", demoWorkspaceService.start(authentication.getName()));
    }

    @PostMapping("/reset")
    public ApiResponse<DemoWorkspaceStatusResponse> reset(Authentication authentication) {
        return ApiResponse.ok("Demo workspace reset.", demoWorkspaceService.reset(authentication.getName()));
    }

    @DeleteMapping
    public ApiResponse<Void> delete(Authentication authentication) {
        demoWorkspaceService.delete(authentication.getName());
        return ApiResponse.ok("Demo workspace deleted.");
    }
}
