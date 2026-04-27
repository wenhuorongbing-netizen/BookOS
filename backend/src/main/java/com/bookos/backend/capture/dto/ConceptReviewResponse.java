package com.bookos.backend.capture.dto;

import java.util.List;

public record ConceptReviewResponse(
        RawCaptureResponse capture,
        List<ReviewedConceptResponse> concepts) {}
