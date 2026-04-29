package com.bookos.backend.ai.controller;

import com.bookos.backend.ai.dto.AISuggestionEditRequest;
import com.bookos.backend.ai.dto.AISuggestionRequest;
import com.bookos.backend.ai.dto.AISuggestionResponse;
import com.bookos.backend.ai.dto.AIProviderStatusResponse;
import com.bookos.backend.ai.service.AISuggestionService;
import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.common.enums.AISuggestionType;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AISuggestionController {

    private final AISuggestionService suggestionService;

    @GetMapping("/api/ai/status")
    public ApiResponse<AIProviderStatusResponse> providerStatus() {
        return ApiResponse.ok("AI provider status loaded.", suggestionService.providerStatus());
    }

    @PostMapping("/api/ai/suggestions/note-summary")
    public ApiResponse<AISuggestionResponse> createNoteSummary(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI note summary draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.NOTE_SUMMARY, request));
    }

    @PostMapping("/api/ai/suggestions/extract-actions")
    public ApiResponse<AISuggestionResponse> extractActions(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI action draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.EXTRACT_ACTIONS, request));
    }

    @PostMapping("/api/ai/suggestions/extract-concepts")
    public ApiResponse<AISuggestionResponse> extractConcepts(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI concept draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.EXTRACT_CONCEPTS, request));
    }

    @PostMapping("/api/ai/suggestions/design-lenses")
    public ApiResponse<AISuggestionResponse> suggestDesignLenses(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI design lens draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.SUGGEST_DESIGN_LENSES, request));
    }

    @PostMapping("/api/ai/suggestions/project-applications")
    public ApiResponse<AISuggestionResponse> suggestProjectApplications(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI project application draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.SUGGEST_PROJECT_APPLICATIONS, request));
    }

    @PostMapping("/api/ai/suggestions/forum-thread")
    public ApiResponse<AISuggestionResponse> suggestForumThread(
            Authentication authentication,
            @Valid @RequestBody(required = false) AISuggestionRequest request) {
        return ApiResponse.ok("AI forum thread draft created.",
                suggestionService.generate(authentication.getName(), AISuggestionType.FORUM_THREAD_DRAFT, request));
    }

    @GetMapping("/api/ai/suggestions")
    public ApiResponse<List<AISuggestionResponse>> listSuggestions(Authentication authentication) {
        return ApiResponse.ok("AI suggestions loaded.", suggestionService.listSuggestions(authentication.getName()));
    }

    @PutMapping("/api/ai/suggestions/{id}/accept")
    public ApiResponse<AISuggestionResponse> acceptSuggestion(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("AI suggestion accepted as a draft decision.",
                suggestionService.acceptSuggestion(authentication.getName(), id));
    }

    @PutMapping("/api/ai/suggestions/{id}/reject")
    public ApiResponse<AISuggestionResponse> rejectSuggestion(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("AI suggestion rejected.",
                suggestionService.rejectSuggestion(authentication.getName(), id));
    }

    @PutMapping("/api/ai/suggestions/{id}/edit")
    public ApiResponse<AISuggestionResponse> editSuggestion(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AISuggestionEditRequest request) {
        return ApiResponse.ok("AI suggestion edited.", suggestionService.editSuggestion(authentication.getName(), id, request));
    }
}
