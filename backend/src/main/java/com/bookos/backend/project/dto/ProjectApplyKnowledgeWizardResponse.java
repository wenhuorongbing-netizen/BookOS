package com.bookos.backend.project.dto;

import java.util.List;

public record ProjectApplyKnowledgeWizardResponse(
        Long projectId,
        String idempotencyKey,
        boolean duplicate,
        String clientStepIntent,
        ProjectProblemResponse projectProblem,
        ProjectApplicationResponse projectApplication,
        DesignDecisionResponse designDecision,
        PlaytestPlanResponse playtestPlan,
        PlaytestFindingResponse playtestFinding,
        ProjectLensReviewResponse lensReview,
        ProjectKnowledgeLinkResponse projectKnowledgeLink,
        List<ProjectWizardCreatedRecordResponse> createdRecords) {

    public ProjectApplyKnowledgeWizardResponse asDuplicate() {
        return new ProjectApplyKnowledgeWizardResponse(
                projectId,
                idempotencyKey,
                true,
                clientStepIntent,
                projectProblem,
                projectApplication,
                designDecision,
                playtestPlan,
                playtestFinding,
                lensReview,
                projectKnowledgeLink,
                createdRecords);
    }
}
