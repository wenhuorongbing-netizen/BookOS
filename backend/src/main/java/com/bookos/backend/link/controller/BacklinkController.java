package com.bookos.backend.link.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.link.dto.BacklinkResponse;
import com.bookos.backend.link.service.BacklinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backlinks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class BacklinkController {

    private final BacklinkService backlinkService;

    @GetMapping
    public ApiResponse<List<BacklinkResponse>> listBacklinks(
            Authentication authentication,
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ApiResponse.ok("Backlinks loaded.", backlinkService.listBacklinks(authentication.getName(), entityType, entityId));
    }
}
