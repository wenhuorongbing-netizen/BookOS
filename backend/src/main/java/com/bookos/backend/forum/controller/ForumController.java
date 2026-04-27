package com.bookos.backend.forum.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.forum.dto.ForumCategoryRequest;
import com.bookos.backend.forum.dto.ForumCategoryResponse;
import com.bookos.backend.forum.dto.ForumCommentRequest;
import com.bookos.backend.forum.dto.ForumCommentResponse;
import com.bookos.backend.forum.dto.ForumReportRequest;
import com.bookos.backend.forum.dto.ForumThreadRequest;
import com.bookos.backend.forum.dto.ForumThreadResponse;
import com.bookos.backend.forum.dto.StructuredPostTemplateResponse;
import com.bookos.backend.forum.service.ForumService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN','MODERATOR')")
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/categories")
    public ApiResponse<List<ForumCategoryResponse>> categories() {
        return ApiResponse.ok("Forum categories loaded.", forumService.listCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<ApiResponse<ForumCategoryResponse>> createCategory(
            Authentication authentication,
            @Valid @RequestBody ForumCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Forum category created.", forumService.createCategory(authentication.getName(), request)));
    }

    @GetMapping("/templates")
    public ApiResponse<List<StructuredPostTemplateResponse>> templates() {
        return ApiResponse.ok("Structured post templates loaded.", forumService.listTemplates());
    }

    @GetMapping("/threads")
    public ApiResponse<List<ForumThreadResponse>> threads(
            Authentication authentication,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok("Forum threads loaded.", forumService.listThreads(authentication.getName(), categorySlug, q));
    }

    @PostMapping("/threads")
    public ResponseEntity<ApiResponse<ForumThreadResponse>> createThread(
            Authentication authentication,
            @Valid @RequestBody ForumThreadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Forum thread created.", forumService.createThread(authentication.getName(), request)));
    }

    @GetMapping("/threads/{id}")
    public ApiResponse<ForumThreadResponse> thread(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum thread loaded.", forumService.getThread(authentication.getName(), id));
    }

    @PutMapping("/threads/{id}")
    public ApiResponse<ForumThreadResponse> updateThread(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ForumThreadRequest request) {
        return ApiResponse.ok("Forum thread updated.", forumService.updateThread(authentication.getName(), id, request));
    }

    @DeleteMapping("/threads/{id}")
    public ApiResponse<Void> deleteThread(Authentication authentication, @PathVariable Long id) {
        forumService.deleteThread(authentication.getName(), id);
        return ApiResponse.ok("Forum thread deleted.");
    }

    @GetMapping("/threads/{id}/comments")
    public ApiResponse<List<ForumCommentResponse>> comments(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum comments loaded.", forumService.listComments(authentication.getName(), id));
    }

    @PostMapping("/threads/{id}/comments")
    public ResponseEntity<ApiResponse<ForumCommentResponse>> createComment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ForumCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Forum comment created.", forumService.createComment(authentication.getName(), id, request)));
    }

    @PutMapping("/comments/{id}")
    public ApiResponse<ForumCommentResponse> updateComment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ForumCommentRequest request) {
        return ApiResponse.ok("Forum comment updated.", forumService.updateComment(authentication.getName(), id, request));
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(Authentication authentication, @PathVariable Long id) {
        forumService.deleteComment(authentication.getName(), id);
        return ApiResponse.ok("Forum comment deleted.");
    }

    @PostMapping("/threads/{id}/bookmark")
    public ApiResponse<ForumThreadResponse> bookmark(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum thread bookmarked.", forumService.bookmark(authentication.getName(), id));
    }

    @DeleteMapping("/threads/{id}/bookmark")
    public ApiResponse<ForumThreadResponse> removeBookmark(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum bookmark removed.", forumService.removeBookmark(authentication.getName(), id));
    }

    @PostMapping("/threads/{id}/like")
    public ApiResponse<ForumThreadResponse> like(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum thread liked.", forumService.like(authentication.getName(), id));
    }

    @DeleteMapping("/threads/{id}/like")
    public ApiResponse<ForumThreadResponse> removeLike(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Forum like removed.", forumService.removeLike(authentication.getName(), id));
    }

    @PostMapping("/threads/{id}/report")
    public ApiResponse<Void> report(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ForumReportRequest request) {
        forumService.report(authentication.getName(), id, request);
        return ApiResponse.ok("Forum thread reported.");
    }
}
