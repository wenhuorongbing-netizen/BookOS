package com.bookos.backend.usecase.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.usecase.dto.UseCaseEventRequest;
import com.bookos.backend.usecase.dto.UseCaseEventResponse;
import com.bookos.backend.usecase.dto.UseCaseProgressResponse;
import com.bookos.backend.usecase.service.UseCaseEventService;
import com.bookos.backend.usecase.service.UseCaseProgressService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/use-cases/progress")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UseCaseProgressController {

    private final UseCaseProgressService progressService;
    private final UseCaseEventService eventService;

    @GetMapping
    public ApiResponse<List<UseCaseProgressResponse>> list(Authentication authentication) {
        return ApiResponse.ok("Use case progress loaded.", progressService.list(authentication.getName()));
    }

    @GetMapping("/{slug}")
    public ApiResponse<UseCaseProgressResponse> get(Authentication authentication, @PathVariable String slug) {
        return ApiResponse.ok("Use case progress loaded.", progressService.get(authentication.getName(), slug));
    }

    @PostMapping("/{slug}/start")
    public ApiResponse<UseCaseProgressResponse> start(Authentication authentication, @PathVariable String slug) {
        return ApiResponse.ok("Use case started.", progressService.start(authentication.getName(), slug));
    }

    @PutMapping("/{slug}/steps/{stepKey}/complete")
    public ApiResponse<UseCaseProgressResponse> completeStep(
            Authentication authentication,
            @PathVariable String slug,
            @PathVariable String stepKey) {
        return ApiResponse.ok("Use case step completed.", progressService.completeStep(authentication.getName(), slug, stepKey));
    }

    @PutMapping("/{slug}/reset")
    public ApiResponse<UseCaseProgressResponse> reset(Authentication authentication, @PathVariable String slug) {
        return ApiResponse.ok("Use case progress reset.", progressService.reset(authentication.getName(), slug));
    }

    @PostMapping("/events")
    public ApiResponse<UseCaseEventResponse> recordEvent(
            Authentication authentication,
            @Valid @RequestBody UseCaseEventRequest request) {
        return ApiResponse.ok("Use case event recorded.", eventService.record(authentication.getName(), request));
    }
}
