package com.bookos.backend.project.controller;

import com.bookos.backend.common.ApiResponse;
import com.bookos.backend.project.dto.ApplySourceRequest;
import com.bookos.backend.project.dto.DesignDecisionRequest;
import com.bookos.backend.project.dto.DesignDecisionResponse;
import com.bookos.backend.project.dto.GameProjectRequest;
import com.bookos.backend.project.dto.GameProjectResponse;
import com.bookos.backend.project.dto.PlaytestFindingRequest;
import com.bookos.backend.project.dto.PlaytestFindingResponse;
import com.bookos.backend.project.dto.PlaytestPlanRequest;
import com.bookos.backend.project.dto.PlaytestPlanResponse;
import com.bookos.backend.project.dto.ProjectApplyKnowledgeWizardRequest;
import com.bookos.backend.project.dto.ProjectApplyKnowledgeWizardResponse;
import com.bookos.backend.project.dto.ProjectApplicationRequest;
import com.bookos.backend.project.dto.ProjectApplicationResponse;
import com.bookos.backend.project.dto.ProjectKnowledgeLinkRequest;
import com.bookos.backend.project.dto.ProjectKnowledgeLinkResponse;
import com.bookos.backend.project.dto.ProjectLensReviewRequest;
import com.bookos.backend.project.dto.ProjectLensReviewResponse;
import com.bookos.backend.project.dto.ProjectProblemRequest;
import com.bookos.backend.project.dto.ProjectProblemResponse;
import com.bookos.backend.project.dto.ProjectPrototypeTaskFromDailyRequest;
import com.bookos.backend.project.service.ProjectService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/api/projects")
    public ApiResponse<List<GameProjectResponse>> listProjects(Authentication authentication) {
        return ApiResponse.ok("Projects loaded.", projectService.listProjects(authentication.getName()));
    }

    @PostMapping("/api/projects")
    public ResponseEntity<ApiResponse<GameProjectResponse>> createProject(
            Authentication authentication,
            @Valid @RequestBody GameProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project created.", projectService.createProject(authentication.getName(), request)));
    }

    @GetMapping("/api/projects/{id}")
    public ApiResponse<GameProjectResponse> getProject(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.ok("Project loaded.", projectService.getProject(authentication.getName(), id));
    }

    @PutMapping("/api/projects/{id}")
    public ApiResponse<GameProjectResponse> updateProject(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody GameProjectRequest request) {
        return ApiResponse.ok("Project updated.", projectService.updateProject(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/projects/{id}")
    public ApiResponse<Void> deleteProject(Authentication authentication, @PathVariable Long id) {
        projectService.archiveProject(authentication.getName(), id);
        return ApiResponse.ok("Project archived.");
    }

    @PutMapping("/api/projects/{id}/archive")
    public ApiResponse<Void> archiveProject(Authentication authentication, @PathVariable Long id) {
        projectService.archiveProject(authentication.getName(), id);
        return ApiResponse.ok("Project archived.");
    }

    @GetMapping("/api/projects/{projectId}/problems")
    public ApiResponse<List<ProjectProblemResponse>> listProblems(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Project problems loaded.", projectService.listProblems(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/problems")
    public ResponseEntity<ApiResponse<ProjectProblemResponse>> createProblem(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project problem created.", projectService.createProblem(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/project-problems/{id}")
    public ApiResponse<ProjectProblemResponse> updateProblem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProjectProblemRequest request) {
        return ApiResponse.ok("Project problem updated.", projectService.updateProblem(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/project-problems/{id}")
    public ApiResponse<Void> deleteProblem(Authentication authentication, @PathVariable Long id) {
        projectService.deleteProblem(authentication.getName(), id);
        return ApiResponse.ok("Project problem deleted.");
    }

    @GetMapping("/api/projects/{projectId}/applications")
    public ApiResponse<List<ProjectApplicationResponse>> listApplications(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Project applications loaded.", projectService.listApplications(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/applications")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> createApplication(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project application created.", projectService.createApplication(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/project-applications/{id}")
    public ApiResponse<ProjectApplicationResponse> updateApplication(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProjectApplicationRequest request) {
        return ApiResponse.ok("Project application updated.", projectService.updateApplication(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/project-applications/{id}")
    public ApiResponse<Void> deleteApplication(Authentication authentication, @PathVariable Long id) {
        projectService.deleteApplication(authentication.getName(), id);
        return ApiResponse.ok("Project application deleted.");
    }

    @GetMapping("/api/projects/{projectId}/decisions")
    public ApiResponse<List<DesignDecisionResponse>> listDecisions(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Design decisions loaded.", projectService.listDecisions(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/decisions")
    public ResponseEntity<ApiResponse<DesignDecisionResponse>> createDecision(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody DesignDecisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Design decision created.", projectService.createDecision(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/design-decisions/{id}")
    public ApiResponse<DesignDecisionResponse> updateDecision(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DesignDecisionRequest request) {
        return ApiResponse.ok("Design decision updated.", projectService.updateDecision(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/design-decisions/{id}")
    public ApiResponse<Void> deleteDecision(Authentication authentication, @PathVariable Long id) {
        projectService.deleteDecision(authentication.getName(), id);
        return ApiResponse.ok("Design decision deleted.");
    }

    @GetMapping("/api/projects/{projectId}/playtest-plans")
    public ApiResponse<List<PlaytestPlanResponse>> listPlaytestPlans(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Playtest plans loaded.", projectService.listPlaytestPlans(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/playtest-plans")
    public ResponseEntity<ApiResponse<PlaytestPlanResponse>> createPlaytestPlan(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody PlaytestPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Playtest plan created.", projectService.createPlaytestPlan(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/playtest-plans/{id}")
    public ApiResponse<PlaytestPlanResponse> updatePlaytestPlan(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PlaytestPlanRequest request) {
        return ApiResponse.ok("Playtest plan updated.", projectService.updatePlaytestPlan(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/playtest-plans/{id}")
    public ApiResponse<Void> deletePlaytestPlan(Authentication authentication, @PathVariable Long id) {
        projectService.deletePlaytestPlan(authentication.getName(), id);
        return ApiResponse.ok("Playtest plan deleted.");
    }

    @GetMapping("/api/projects/{projectId}/playtest-findings")
    public ApiResponse<List<PlaytestFindingResponse>> listPlaytestFindings(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Playtest findings loaded.", projectService.listPlaytestFindings(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/playtest-findings")
    public ResponseEntity<ApiResponse<PlaytestFindingResponse>> createPlaytestFinding(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody PlaytestFindingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Playtest finding created.", projectService.createPlaytestFinding(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/playtest-findings/{id}")
    public ApiResponse<PlaytestFindingResponse> updatePlaytestFinding(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PlaytestFindingRequest request) {
        return ApiResponse.ok("Playtest finding updated.", projectService.updatePlaytestFinding(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/playtest-findings/{id}")
    public ApiResponse<Void> deletePlaytestFinding(Authentication authentication, @PathVariable Long id) {
        projectService.deletePlaytestFinding(authentication.getName(), id);
        return ApiResponse.ok("Playtest finding deleted.");
    }

    @GetMapping("/api/projects/{projectId}/knowledge-links")
    public ApiResponse<List<ProjectKnowledgeLinkResponse>> listKnowledgeLinks(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Project knowledge links loaded.", projectService.listKnowledgeLinks(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/knowledge-links")
    public ResponseEntity<ApiResponse<ProjectKnowledgeLinkResponse>> createKnowledgeLink(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectKnowledgeLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project knowledge link created.", projectService.createKnowledgeLink(authentication.getName(), projectId, request)));
    }

    @DeleteMapping("/api/project-knowledge-links/{id}")
    public ApiResponse<Void> deleteKnowledgeLink(Authentication authentication, @PathVariable Long id) {
        projectService.deleteKnowledgeLink(authentication.getName(), id);
        return ApiResponse.ok("Project knowledge link deleted.");
    }

    @GetMapping("/api/projects/{projectId}/lens-reviews")
    public ApiResponse<List<ProjectLensReviewResponse>> listLensReviews(Authentication authentication, @PathVariable Long projectId) {
        return ApiResponse.ok("Project lens reviews loaded.", projectService.listLensReviews(authentication.getName(), projectId));
    }

    @PostMapping("/api/projects/{projectId}/lens-reviews")
    public ResponseEntity<ApiResponse<ProjectLensReviewResponse>> createLensReview(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectLensReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project lens review created.", projectService.createLensReview(authentication.getName(), projectId, request)));
    }

    @PutMapping("/api/project-lens-reviews/{id}")
    public ApiResponse<ProjectLensReviewResponse> updateLensReview(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProjectLensReviewRequest request) {
        return ApiResponse.ok("Project lens review updated.", projectService.updateLensReview(authentication.getName(), id, request));
    }

    @DeleteMapping("/api/project-lens-reviews/{id}")
    public ApiResponse<Void> deleteLensReview(Authentication authentication, @PathVariable Long id) {
        projectService.deleteLensReview(authentication.getName(), id);
        return ApiResponse.ok("Project lens review deleted.");
    }

    @PostMapping("/api/projects/{projectId}/apply/source-reference")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> applySourceReference(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ApplySourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Source reference applied to project.", projectService.applySourceReference(authentication.getName(), projectId, request)));
    }

    @PostMapping("/api/projects/{projectId}/apply/quote")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> applyQuote(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ApplySourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Quote applied to project.", projectService.applyQuote(authentication.getName(), projectId, request)));
    }

    @PostMapping("/api/projects/{projectId}/apply/concept")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> applyConcept(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ApplySourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Concept applied to project.", projectService.applyConcept(authentication.getName(), projectId, request)));
    }

    @PostMapping("/api/projects/{projectId}/apply/knowledge-object")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> applyKnowledgeObject(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ApplySourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Knowledge object applied to project.", projectService.applyKnowledgeObject(authentication.getName(), projectId, request)));
    }

    @PostMapping("/api/projects/{projectId}/create-prototype-task-from-daily")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> createPrototypeTaskFromDaily(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectPrototypeTaskFromDailyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Prototype task created from daily prompt.", projectService.createPrototypeTaskFromDaily(authentication.getName(), projectId, request)));
    }

    @PostMapping("/api/projects/{projectId}/wizard/apply-knowledge")
    public ResponseEntity<ApiResponse<ProjectApplyKnowledgeWizardResponse>> applyKnowledgeWizard(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectApplyKnowledgeWizardRequest request) {
        ProjectApplyKnowledgeWizardResponse response = projectService.applyKnowledgeWizard(authentication.getName(), projectId, request);
        HttpStatus status = response.duplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        String message = response.duplicate()
                ? "Project wizard submission already exists; previous result returned."
                : "Project wizard records created transactionally.";
        return ResponseEntity.status(status).body(ApiResponse.ok(message, response));
    }
}
