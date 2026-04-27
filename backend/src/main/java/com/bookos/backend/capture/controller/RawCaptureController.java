package com.bookos.backend.capture.controller;

import com.bookos.backend.capture.dto.CaptureConversionResponse;
import com.bookos.backend.capture.dto.ConceptReviewRequest;
import com.bookos.backend.capture.dto.ConceptReviewResponse;
import com.bookos.backend.capture.dto.RawCaptureConvertRequest;
import com.bookos.backend.capture.dto.RawCaptureRequest;
import com.bookos.backend.capture.dto.RawCaptureResponse;
import com.bookos.backend.capture.dto.RawCaptureUpdateRequest;
import com.bookos.backend.capture.service.RawCaptureService;
import com.bookos.backend.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class RawCaptureController {

    private final RawCaptureService rawCaptureService;

    @PostMapping
    public ResponseEntity<ApiResponse<RawCaptureResponse>> createCapture(
            Authentication authentication,
            @Valid @RequestBody RawCaptureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Capture saved.", rawCaptureService.createCapture(authentication.getName(), request)));
    }

    @GetMapping("/inbox")
    public ApiResponse<List<RawCaptureResponse>> listInbox(
            Authentication authentication,
            @RequestParam(required = false) Long bookId) {
        return ApiResponse.ok("Capture inbox loaded.", rawCaptureService.listInbox(authentication.getName(), bookId));
    }

    @GetMapping("/{id}")
    public ApiResponse<RawCaptureResponse> getCapture(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Capture loaded.", rawCaptureService.getCapture(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<RawCaptureResponse> updateCapture(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RawCaptureUpdateRequest request) {
        return ApiResponse.ok("Capture updated.", rawCaptureService.updateCapture(authentication.getName(), id, request));
    }

    @PostMapping("/{id}/convert")
    public ApiResponse<CaptureConversionResponse> convertCapture(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RawCaptureConvertRequest request) {
        RawCaptureConvertRequest safeRequest = request == null ? new RawCaptureConvertRequest(null, null) : request;
        return ApiResponse.ok("Capture converted.", rawCaptureService.convertCapture(authentication.getName(), id, safeRequest));
    }

    @PostMapping("/{id}/review/concepts")
    public ApiResponse<ConceptReviewResponse> reviewCaptureConcepts(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ConceptReviewRequest request) {
        return ApiResponse.ok("Capture concepts reviewed.", rawCaptureService.reviewCaptureConcepts(authentication.getName(), id, request));
    }

    @PutMapping("/{id}/archive")
    public ApiResponse<RawCaptureResponse> archiveCapture(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Capture archived.", rawCaptureService.archiveCapture(authentication.getName(), id));
    }
}
