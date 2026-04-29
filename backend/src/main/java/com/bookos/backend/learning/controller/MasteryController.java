package com.bookos.backend.learning.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.learning.dto.KnowledgeMasteryRequest;
import com.bookos.backend.learning.dto.KnowledgeMasteryResponse;
import com.bookos.backend.learning.service.LearningService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MasteryController {

    private final LearningService learningService;

    @GetMapping("/api/mastery")
    public ApiResponse<List<KnowledgeMasteryResponse>> list(Authentication authentication) {
        return ApiResponse.ok("Mastery targets loaded.", learningService.listMastery(authentication.getName()));
    }

    @GetMapping("/api/mastery/target")
    public ApiResponse<KnowledgeMasteryResponse> getTarget(
            Authentication authentication,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ApiResponse.ok("Mastery target loaded.", learningService.getMasteryTarget(authentication.getName(), targetType, targetId));
    }

    @PutMapping("/api/mastery/target")
    public ApiResponse<KnowledgeMasteryResponse> updateTarget(
            Authentication authentication,
            @Valid @RequestBody KnowledgeMasteryRequest request) {
        return ApiResponse.ok("Mastery target updated.", learningService.updateMasteryTarget(authentication.getName(), request));
    }
}
