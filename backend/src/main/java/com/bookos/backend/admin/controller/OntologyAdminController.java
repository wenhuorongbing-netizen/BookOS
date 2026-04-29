package com.bookos.backend.admin.controller;

import com.bookos.backend.admin.dto.OntologyImportRequest;
import com.bookos.backend.admin.dto.OntologyImportResponse;
import com.bookos.backend.admin.service.OntologyImportService;
import com.bookos.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ontology")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OntologyAdminController {

    private final OntologyImportService ontologyImportService;

    @GetMapping("/default")
    public ApiResponse<OntologyImportRequest> defaultSeed() {
        return ApiResponse.ok("Default game design ontology seed loaded.", ontologyImportService.defaultSeed());
    }

    @PostMapping("/import/default")
    public ApiResponse<OntologyImportResponse> importDefault(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        return ApiResponse.ok(
                dryRun ? "Default ontology seed validated." : "Default ontology seed imported.",
                ontologyImportService.importDefault(authentication.getName(), dryRun));
    }

    @PostMapping("/import")
    public ApiResponse<OntologyImportResponse> importSeed(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @Valid @RequestBody OntologyImportRequest request) {
        return ApiResponse.ok(
                dryRun ? "Ontology seed payload validated." : "Ontology seed payload imported.",
                ontologyImportService.importSeed(authentication.getName(), request, dryRun));
    }
}
