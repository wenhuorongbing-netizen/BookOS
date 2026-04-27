package com.bookos.backend.daily.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.daily.dto.CreatePrototypeTaskRequest;
import com.bookos.backend.daily.dto.DailyActionRequest;
import com.bookos.backend.daily.dto.DailyHistoryResponse;
import com.bookos.backend.daily.dto.DailyReflectionRequest;
import com.bookos.backend.daily.dto.DailyReflectionResponse;
import com.bookos.backend.daily.dto.DailyTodayResponse;
import com.bookos.backend.daily.service.DailyService;
import com.bookos.backend.knowledge.dto.KnowledgeObjectResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class DailyController {

    private final DailyService dailyService;

    @GetMapping("/today")
    public ApiResponse<DailyTodayResponse> today(Authentication authentication) {
        return ApiResponse.ok("Daily resurfacing loaded.", dailyService.getToday(authentication.getName()));
    }

    @PostMapping("/regenerate")
    public ApiResponse<DailyTodayResponse> regenerate(
            Authentication authentication,
            @Valid @RequestBody DailyActionRequest request) {
        return ApiResponse.ok("Daily item regenerated.", dailyService.regenerate(authentication.getName(), request.target()));
    }

    @PostMapping("/skip")
    public ApiResponse<DailyTodayResponse> skip(
            Authentication authentication,
            @Valid @RequestBody DailyActionRequest request) {
        return ApiResponse.ok("Daily item skipped.", dailyService.skip(authentication.getName(), request.target()));
    }

    @PostMapping("/reflections")
    public ResponseEntity<ApiResponse<DailyReflectionResponse>> saveReflection(
            Authentication authentication,
            @Valid @RequestBody DailyReflectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Daily reflection saved.",
                        dailyService.saveReflection(authentication.getName(), request)));
    }

    @GetMapping("/history")
    public ApiResponse<List<DailyHistoryResponse>> history(Authentication authentication) {
        return ApiResponse.ok("Daily history loaded.", dailyService.history(authentication.getName()));
    }

    @PostMapping("/create-prototype-task")
    public ResponseEntity<ApiResponse<KnowledgeObjectResponse>> createPrototypeTask(
            Authentication authentication,
            @Valid @RequestBody CreatePrototypeTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Prototype task created from daily prompt.",
                        dailyService.createPrototypeTask(authentication.getName(), request)));
    }
}
