package com.bookos.backend.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProjectApplyKnowledgeWizardRequest(
        @Size(max = 64, message = "Source type must be at most 64 characters.")
        String sourceType,

        @Positive(message = "Source id must be positive.")
        Long sourceId,

        @Positive(message = "Source reference id must be positive.")
        Long sourceReferenceId,

        @Valid
        ProjectProblemRequest projectProblem,

        @Valid
        ProjectApplicationRequest projectApplication,

        @Valid
        DesignDecisionRequest designDecision,

        @Valid
        PlaytestPlanRequest playtestPlan,

        @Valid
        PlaytestFindingRequest playtestFinding,

        @Valid
        ProjectLensReviewRequest lensReview,

        @Valid
        ProjectKnowledgeLinkRequest projectKnowledgeLink,

        @Size(max = 500, message = "Client step intent must be at most 500 characters.")
        String clientStepIntent,

        @NotBlank(message = "Idempotency key is required.")
        @Size(max = 120, message = "Idempotency key must be at most 120 characters.")
        String idempotencyKey) {}
