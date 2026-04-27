package com.bookos.backend.link.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.link.dto.EntityLinkRequest;
import com.bookos.backend.link.dto.EntityLinkResponse;
import com.bookos.backend.link.service.EntityLinkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entity-links")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class EntityLinkController {

    private final EntityLinkService entityLinkService;

    @GetMapping
    public ApiResponse<List<EntityLinkResponse>> listEntityLinks(
            Authentication authentication,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId) {
        return ApiResponse.ok(
                "Entity links loaded.",
                entityLinkService.listEntityLinks(authentication.getName(), sourceType, sourceId, targetType, targetId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EntityLinkResponse>> createEntityLink(
            Authentication authentication,
            @Valid @RequestBody EntityLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Entity link created.", entityLinkService.createEntityLink(authentication.getName(), request)));
    }
}
