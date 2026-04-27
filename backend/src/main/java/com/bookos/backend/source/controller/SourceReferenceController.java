package com.bookos.backend.source.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.service.SourceReferenceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class SourceReferenceController {

    private final SourceReferenceService sourceReferenceService;

    @GetMapping("/api/source-references/{id}")
    public ApiResponse<SourceReferenceResponse> getSourceReference(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Source reference loaded.", sourceReferenceService.getSourceReference(authentication.getName(), id));
    }

    @GetMapping("/api/source-references")
    public ApiResponse<List<SourceReferenceResponse>> listSourceReferences(
            Authentication authentication,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        return ApiResponse.ok(
                "Source references loaded.",
                sourceReferenceService.listSourceReferences(authentication.getName(), bookId, entityType, entityId));
    }

    @GetMapping("/api/books/{bookId}/source-references")
    public ApiResponse<List<SourceReferenceResponse>> listBookSources(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Book source references loaded.", sourceReferenceService.listBookSources(authentication.getName(), bookId));
    }

    @GetMapping("/api/notes/{noteId}/source-references")
    public ApiResponse<List<SourceReferenceResponse>> listNoteSources(Authentication authentication, @PathVariable Long noteId) {
        return ApiResponse.ok("Note source references loaded.", sourceReferenceService.listNoteSources(authentication.getName(), noteId));
    }

    @GetMapping("/api/captures/{captureId}/source-references")
    public ApiResponse<List<SourceReferenceResponse>> listCaptureSources(Authentication authentication, @PathVariable Long captureId) {
        return ApiResponse.ok("Capture source references loaded.", sourceReferenceService.listCaptureSources(authentication.getName(), captureId));
    }
}
