package com.bookos.backend.backup.controller;

import com.bookos.backend.backup.dto.ImportCommitResponse;
import com.bookos.backend.backup.dto.ImportPreviewResponse;
import com.bookos.backend.backup.dto.ImportRequest;
import com.bookos.backend.backup.service.ImportExportService;
import com.bookos.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class DataImportController {

    private final ImportExportService importExportService;

    @PostMapping("/api/import/preview")
    public ApiResponse<ImportPreviewResponse> previewImport(
            Authentication authentication,
            @Valid @RequestBody ImportRequest request) {
        return ApiResponse.ok("Import preview created.", importExportService.previewImport(authentication.getName(), request));
    }

    @PostMapping("/api/import/commit")
    public ResponseEntity<ApiResponse<ImportCommitResponse>> commitImport(
            Authentication authentication,
            @Valid @RequestBody ImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Import committed.", importExportService.commitImport(authentication.getName(), request)));
    }
}
