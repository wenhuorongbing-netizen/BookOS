package com.bookos.backend.graph.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.graph.dto.GraphResponse;
import com.bookos.backend.graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/api/graph/book/{bookId}")
    public ApiResponse<GraphResponse> getBookGraph(Authentication authentication, @PathVariable Long bookId) {
        return ApiResponse.ok("Book graph loaded.", graphService.getBookGraph(authentication.getName(), bookId));
    }

    @GetMapping("/api/graph/concept/{conceptId}")
    public ApiResponse<GraphResponse> getConceptGraph(Authentication authentication, @PathVariable Long conceptId) {
        return ApiResponse.ok("Concept graph loaded.", graphService.getConceptGraph(authentication.getName(), conceptId));
    }
}
