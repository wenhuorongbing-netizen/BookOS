package com.bookos.backend.knowledge.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.knowledge.dto.ConceptResponse;
import com.bookos.backend.knowledge.service.ConceptService;
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
public class BookConceptController {

    private final ConceptService conceptService;

    @GetMapping("/api/books/{bookId}/concepts")
    public ApiResponse<List<ConceptResponse>> listBookConcepts(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestParam(required = false) String layer,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok("Book concepts loaded.", conceptService.listConcepts(authentication.getName(), bookId, layer, q));
    }
}
