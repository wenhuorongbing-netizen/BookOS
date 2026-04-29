package com.bookos.backend.learning.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.learning.dto.ReadingSessionFinishRequest;
import com.bookos.backend.learning.dto.ReadingSessionResponse;
import com.bookos.backend.learning.dto.ReadingSessionStartRequest;
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
public class ReadingSessionController {

    private final LearningService learningService;

    @GetMapping("/api/reading-sessions")
    public ApiResponse<List<ReadingSessionResponse>> list(Authentication authentication) {
        return ApiResponse.ok("Reading sessions loaded.", learningService.listReadingSessions(authentication.getName()));
    }

    @PostMapping("/api/reading-sessions/start")
    public ResponseEntity<ApiResponse<ReadingSessionResponse>> start(
            Authentication authentication,
            @Valid @RequestBody ReadingSessionStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Reading session started.", learningService.startReadingSession(authentication.getName(), request)));
    }

    @PutMapping("/api/reading-sessions/{id}/finish")
    public ApiResponse<ReadingSessionResponse> finish(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReadingSessionFinishRequest request) {
        return ApiResponse.ok("Reading session finished.", learningService.finishReadingSession(authentication.getName(), id, request));
    }

    @GetMapping("/api/books/{bookId}/reading-sessions")
    public ApiResponse<List<ReadingSessionResponse>> listForBook(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Book reading sessions loaded.", learningService.listBookReadingSessions(authentication.getName(), bookId));
    }
}
