package com.bookos.backend.search.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.search.dto.SearchResultResponse;
import com.bookos.backend.search.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/search")
    public ApiResponse<List<SearchResultResponse>> search(
            Authentication authentication,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long bookId) {
        return ApiResponse.ok("Search results loaded.", searchService.search(authentication.getName(), q, type, bookId));
    }
}
