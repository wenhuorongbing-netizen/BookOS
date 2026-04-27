package com.bookos.backend.book.controller;

import com.bookos.backend.book.dto.UpdateUserBookProgressRequest;
import com.bookos.backend.book.dto.UpdateUserBookRatingRequest;
import com.bookos.backend.book.dto.UpdateUserBookStatusRequest;
import com.bookos.backend.book.dto.UserBookResponse;
import com.bookos.backend.book.service.UserLibraryService;
import com.bookos.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-books")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UserBookController {

    private final UserLibraryService userLibraryService;

    @GetMapping
    public ApiResponse<List<UserBookResponse>> listLibrary(Authentication authentication) {
        return ApiResponse.ok("Library loaded.", userLibraryService.listLibrary(authentication.getName()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserBookResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserBookStatusRequest request) {
        return ApiResponse.ok("Reading status updated.", userLibraryService.updateStatus(authentication.getName(), id, request));
    }

    @PutMapping("/{id}/progress")
    public ApiResponse<UserBookResponse> updateProgress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserBookProgressRequest request) {
        return ApiResponse.ok("Reading progress updated.", userLibraryService.updateProgress(authentication.getName(), id, request));
    }

    @PutMapping("/{id}/rating")
    public ApiResponse<UserBookResponse> updateRating(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserBookRatingRequest request) {
        return ApiResponse.ok("Book rating updated.", userLibraryService.updateRating(authentication.getName(), id, request));
    }

    @GetMapping("/currently-reading")
    public ApiResponse<List<UserBookResponse>> currentlyReading(Authentication authentication) {
        return ApiResponse.ok("Currently reading books loaded.", userLibraryService.currentlyReading(authentication.getName()));
    }

    @GetMapping("/five-star")
    public ApiResponse<List<UserBookResponse>> fiveStar(Authentication authentication) {
        return ApiResponse.ok("Five-star books loaded.", userLibraryService.fiveStar(authentication.getName()));
    }

    @GetMapping("/anti-library")
    public ApiResponse<List<UserBookResponse>> antiLibrary(Authentication authentication) {
        return ApiResponse.ok("Anti-library books loaded.", userLibraryService.antiLibrary(authentication.getName()));
    }
}
