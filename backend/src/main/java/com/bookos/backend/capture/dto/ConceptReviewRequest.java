package com.bookos.backend.capture.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ConceptReviewRequest(
        @NotEmpty(message = "At least one concept review decision is required.")
        List<@Valid ConceptReviewItemRequest> concepts) {}
