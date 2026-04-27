package com.bookos.backend.action.controller;

import com.bookos.backend.action.dto.ActionItemRequest;
import com.bookos.backend.action.dto.ActionItemResponse;
import com.bookos.backend.action.service.ActionItemService;
import com.bookos.backend.capture.dto.CaptureConversionResponse;
import com.bookos.backend.capture.dto.CaptureConversionTarget;
import com.bookos.backend.capture.dto.RawCaptureConvertRequest;
import com.bookos.backend.capture.service.RawCaptureService;
import com.bookos.backend.common.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ActionItemController {

    private final ActionItemService actionItemService;
    private final RawCaptureService rawCaptureService;

    @GetMapping("/api/action-items")
    public ApiResponse<List<ActionItemResponse>> listActionItems(
            Authentication authentication,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok("Action items loaded.", actionItemService.listActionItems(authentication.getName(), bookId, completed, q));
    }

    @PostMapping("/api/action-items")
    public ResponseEntity<ApiResponse<ActionItemResponse>> createActionItem(
            Authentication authentication,
            @Valid @RequestBody ActionItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Action item created.", actionItemService.createActionItem(authentication.getName(), request)));
    }

    @GetMapping("/api/action-items/{id}")
    public ApiResponse<ActionItemResponse> getActionItem(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Action item loaded.", actionItemService.getActionItem(authentication.getName(), id));
    }

    @PutMapping("/api/action-items/{id}")
    public ApiResponse<ActionItemResponse> updateActionItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ActionItemRequest request) {
        return ApiResponse.ok("Action item updated.", actionItemService.updateActionItem(authentication.getName(), id, request));
    }

    @PutMapping("/api/action-items/{id}/complete")
    public ApiResponse<ActionItemResponse> completeActionItem(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Action item completed.", actionItemService.completeActionItem(authentication.getName(), id));
    }

    @PutMapping("/api/action-items/{id}/reopen")
    public ApiResponse<ActionItemResponse> reopenActionItem(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Action item reopened.", actionItemService.reopenActionItem(authentication.getName(), id));
    }

    @DeleteMapping("/api/action-items/{id}")
    public ApiResponse<Void> archiveActionItem(Authentication authentication, @PathVariable Long id) {
        actionItemService.archiveActionItem(authentication.getName(), id);
        return ApiResponse.ok("Action item archived.");
    }

    @PostMapping("/api/captures/{id}/convert/action-item")
    public ApiResponse<CaptureConversionResponse> convertCaptureToActionItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) RawCaptureConvertRequest request) {
        String title = request == null ? null : request.title();
        return ApiResponse.ok(
                "Capture converted to action item.",
                rawCaptureService.convertCapture(
                        authentication.getName(), id, new RawCaptureConvertRequest(CaptureConversionTarget.ACTION_ITEM, title)));
    }
}
