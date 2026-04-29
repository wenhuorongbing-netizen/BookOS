package com.bookos.backend.learning.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.learning.dto.ReviewGenerateRequest;
import com.bookos.backend.learning.dto.ReviewItemRequest;
import com.bookos.backend.learning.dto.ReviewItemResponse;
import com.bookos.backend.learning.dto.ReviewItemUpdateRequest;
import com.bookos.backend.learning.dto.ReviewSessionRequest;
import com.bookos.backend.learning.dto.ReviewSessionResponse;
import com.bookos.backend.learning.service.LearningService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final LearningService learningService;

    @GetMapping("/api/review/sessions")
    public ApiResponse<List<ReviewSessionResponse>> list(Authentication authentication) {
        return ApiResponse.ok("Review sessions loaded.", learningService.listReviewSessions(authentication.getName()));
    }

    @PostMapping("/api/review/sessions")
    public ResponseEntity<ApiResponse<ReviewSessionResponse>> create(
            Authentication authentication,
            @Valid @RequestBody ReviewSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review session created.", learningService.createReviewSession(authentication.getName(), request)));
    }

    @GetMapping("/api/review/sessions/{id}")
    public ApiResponse<ReviewSessionResponse> get(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Review session loaded.", learningService.getReviewSession(authentication.getName(), id));
    }

    @PostMapping("/api/review/sessions/{id}/items")
    public ResponseEntity<ApiResponse<ReviewItemResponse>> addItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReviewItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review item created.", learningService.addReviewItem(authentication.getName(), id, request)));
    }

    @PutMapping("/api/review/items/{id}")
    public ApiResponse<ReviewItemResponse> updateItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReviewItemUpdateRequest request) {
        return ApiResponse.ok("Review item updated.", learningService.updateReviewItem(authentication.getName(), id, request));
    }

    @PostMapping("/api/review/generate-from-book")
    public ResponseEntity<ApiResponse<ReviewSessionResponse>> generateFromBook(
            Authentication authentication,
            @Valid @RequestBody ReviewGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book review session generated.", learningService.generateReviewFromBook(authentication.getName(), request)));
    }

    @PostMapping("/api/review/generate-from-concept")
    public ResponseEntity<ApiResponse<ReviewSessionResponse>> generateFromConcept(
            Authentication authentication,
            @Valid @RequestBody ReviewGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Concept review session generated.", learningService.generateReviewFromConcept(authentication.getName(), request)));
    }

    @PostMapping("/api/review/generate-from-project")
    public ResponseEntity<ApiResponse<ReviewSessionResponse>> generateFromProject(
            Authentication authentication,
            @Valid @RequestBody ReviewGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project review session generated.", learningService.generateReviewFromProject(authentication.getName(), request)));
    }
}
