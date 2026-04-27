package com.bookos.backend.quote.controller;

import com.bookos.backend.capture.dto.CaptureConversionResponse;
import com.bookos.backend.capture.dto.CaptureConversionTarget;
import com.bookos.backend.capture.dto.RawCaptureConvertRequest;
import com.bookos.backend.capture.service.RawCaptureService;
import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.quote.dto.QuoteRequest;
import com.bookos.backend.quote.dto.QuoteResponse;
import com.bookos.backend.quote.service.QuoteService;
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
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class QuoteController {

    private final QuoteService quoteService;
    private final RawCaptureService rawCaptureService;

    @GetMapping("/api/quotes")
    public ApiResponse<List<QuoteResponse>> listQuotes(
            Authentication authentication,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String q) {
        return ApiResponse.ok("Quotes loaded.", quoteService.listQuotes(authentication.getName(), bookId, q));
    }

    @PostMapping("/api/quotes")
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(
            Authentication authentication,
            @Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Quote created.", quoteService.createQuote(authentication.getName(), request)));
    }

    @GetMapping("/api/quotes/{id}")
    public ApiResponse<QuoteResponse> getQuote(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Quote loaded.", quoteService.getQuote(authentication.getName(), id));
    }

    @PutMapping("/api/quotes/{id}")
    public ApiResponse<QuoteResponse> updateQuote(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request) {
        return ApiResponse.ok("Quote updated.", quoteService.updateQuote(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/quotes/{id}")
    public ApiResponse<Void> archiveQuote(Authentication authentication, @PathVariable Long id) {
        quoteService.archiveQuote(authentication.getName(), id);
        return ApiResponse.ok("Quote archived.");
    }

    @PostMapping("/api/captures/{id}/convert/quote")
    public ApiResponse<CaptureConversionResponse> convertCaptureToQuote(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok(
                "Capture converted to quote.",
                rawCaptureService.convertCapture(authentication.getName(), id, new RawCaptureConvertRequest(CaptureConversionTarget.QUOTE, null)));
    }
}
