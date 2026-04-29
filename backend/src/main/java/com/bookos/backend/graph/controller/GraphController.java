package com.bookos.backend.graph.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.graph.dto.GraphResponse;
import com.bookos.backend.graph.service.GraphService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/api/graph")
    public ApiResponse<GraphResponse> getWorkspaceGraph(
            Authentication authentication,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long conceptId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String relationshipType,
            @RequestParam(required = false) SourceConfidence sourceConfidence,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(
                "Workspace graph loaded.",
                graphService.getWorkspaceGraph(
                        authentication.getName(),
                        bookId,
                        conceptId,
                        projectId,
                        entityType,
                        relationshipType,
                        sourceConfidence,
                        createdFrom,
                        createdTo,
                        depth,
                        limit));
    }

    @GetMapping("/api/graph/book/{bookId}")
    public ApiResponse<GraphResponse> getBookGraph(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String relationshipType,
            @RequestParam(required = false) SourceConfidence sourceConfidence,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(
                "Book graph loaded.",
                graphService.getBookGraph(
                        authentication.getName(),
                        bookId,
                        entityType,
                        relationshipType,
                        sourceConfidence,
                        createdFrom,
                        createdTo,
                        depth,
                        limit));
    }

    @GetMapping("/api/graph/concept/{conceptId}")
    public ApiResponse<GraphResponse> getConceptGraph(
            Authentication authentication,
            @PathVariable Long conceptId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String relationshipType,
            @RequestParam(required = false) SourceConfidence sourceConfidence,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(
                "Concept graph loaded.",
                graphService.getConceptGraph(
                        authentication.getName(),
                        conceptId,
                        entityType,
                        relationshipType,
                        sourceConfidence,
                        createdFrom,
                        createdTo,
                        depth,
                        limit));
    }

    @GetMapping("/api/graph/project/{projectId}")
    public ApiResponse<GraphResponse> getProjectGraph(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String relationshipType,
            @RequestParam(required = false) SourceConfidence sourceConfidence,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) Integer depth,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(
                "Project graph loaded.",
                graphService.getProjectGraph(
                        authentication.getName(),
                        projectId,
                        entityType,
                        relationshipType,
                        sourceConfidence,
                        createdFrom,
                        createdTo,
                        depth,
                        limit));
    }
}
