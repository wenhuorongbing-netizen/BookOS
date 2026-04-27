package com.bookos.backend.knowledge.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.knowledge.dto.KnowledgeObjectRequest;
import com.bookos.backend.knowledge.dto.KnowledgeObjectResponse;
import com.bookos.backend.knowledge.service.KnowledgeObjectService;
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
@RequestMapping("/api/knowledge-objects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class KnowledgeObjectController {

    private final KnowledgeObjectService knowledgeObjectService;

    @GetMapping
    public ApiResponse<List<KnowledgeObjectResponse>> listKnowledgeObjects(
            Authentication authentication,
            @RequestParam(required = false) KnowledgeObjectType type,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long conceptId,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok(
                "Knowledge objects loaded.",
                knowledgeObjectService.listKnowledgeObjects(authentication.getName(), type, bookId, conceptId, q));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KnowledgeObjectResponse>> createKnowledgeObject(
            Authentication authentication,
            @Valid @RequestBody KnowledgeObjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Knowledge object created.",
                        knowledgeObjectService.createKnowledgeObject(authentication.getName(), request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeObjectResponse> getKnowledgeObject(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Knowledge object loaded.", knowledgeObjectService.getKnowledgeObject(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeObjectResponse> updateKnowledgeObject(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeObjectRequest request) {
        return ApiResponse.ok(
                "Knowledge object updated.",
                knowledgeObjectService.updateKnowledgeObject(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> archiveKnowledgeObject(Authentication authentication, @PathVariable Long id) {
        knowledgeObjectService.archiveKnowledgeObject(authentication.getName(), id);
        return ApiResponse.ok("Knowledge object archived.");
    }
}
