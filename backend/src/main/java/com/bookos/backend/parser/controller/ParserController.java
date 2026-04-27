package com.bookos.backend.parser.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import com.bookos.backend.parser.dto.ParserPreviewRequest;
import com.bookos.backend.parser.service.NoteParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ParserController {

    private final NoteParserService parserService;

    @PostMapping("/preview")
    public ApiResponse<ParsedNoteResponse> preview(@Valid @RequestBody ParserPreviewRequest request) {
        return ApiResponse.ok("Parser preview generated.", parserService.parse(request.rawText()));
    }
}
