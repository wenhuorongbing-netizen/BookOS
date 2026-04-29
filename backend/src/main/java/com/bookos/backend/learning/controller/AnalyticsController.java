package com.bookos.backend.learning.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.learning.dto.BookAnalyticsResponse;
import com.bookos.backend.learning.dto.KnowledgeAnalyticsResponse;
import com.bookos.backend.learning.dto.ReadingAnalyticsResponse;
import com.bookos.backend.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final LearningService learningService;

    @GetMapping("/api/analytics/reading")
    public ApiResponse<ReadingAnalyticsResponse> reading(Authentication authentication) {
        return ApiResponse.ok("Reading analytics loaded.", learningService.readingAnalytics(authentication.getName()));
    }

    @GetMapping("/api/analytics/knowledge")
    public ApiResponse<KnowledgeAnalyticsResponse> knowledge(Authentication authentication) {
        return ApiResponse.ok("Knowledge analytics loaded.", learningService.knowledgeAnalytics(authentication.getName()));
    }

    @GetMapping("/api/analytics/books/{bookId}")
    public ApiResponse<BookAnalyticsResponse> book(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Book analytics loaded.", learningService.bookAnalytics(authentication.getName(), bookId));
    }
}
