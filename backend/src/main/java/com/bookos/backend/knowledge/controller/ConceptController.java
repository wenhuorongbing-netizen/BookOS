package com.bookos.backend.knowledge.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.knowledge.dto.ConceptRequest;
import com.bookos.backend.knowledge.dto.ConceptResponse;
import com.bookos.backend.knowledge.service.ConceptService;
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
@RequestMapping("/api/concepts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ConceptController {

    private final ConceptService conceptService;

    @GetMapping
    public ApiResponse<List<ConceptResponse>> listConcepts(
            Authentication authentication,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String layer,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok("Concepts loaded.", conceptService.listConcepts(authentication.getName(), bookId, layer, q));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConceptResponse>> createConcept(
            Authentication authentication,
            @Valid @RequestBody ConceptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Concept created.", conceptService.createConcept(authentication.getName(), request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConceptResponse> getConcept(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Concept loaded.", conceptService.getConcept(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConceptResponse> updateConcept(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ConceptRequest request) {
        return ApiResponse.ok("Concept updated.", conceptService.updateConcept(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> archiveConcept(Authentication authentication, @PathVariable Long id) {
        conceptService.archiveConcept(authentication.getName(), id);
        return ApiResponse.ok("Concept archived.");
    }
}
