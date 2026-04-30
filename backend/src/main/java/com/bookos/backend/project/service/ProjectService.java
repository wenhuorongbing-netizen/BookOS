package com.bookos.backend.project.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.service.ActionItemService;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.daily.entity.DailyDesignPrompt;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
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
import com.bookos.backend.project.dto.ProjectWizardCreatedRecordResponse;
import com.bookos.backend.project.entity.DesignDecision;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.PlaytestFinding;
import com.bookos.backend.project.entity.PlaytestPlan;
import com.bookos.backend.project.entity.PlaytestSession;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.entity.ProjectKnowledgeLink;
import com.bookos.backend.project.entity.ProjectLensReview;
import com.bookos.backend.project.entity.ProjectProblem;
import com.bookos.backend.project.entity.ProjectWizardSubmission;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.PlaytestPlanRepository;
import com.bookos.backend.project.repository.PlaytestSessionRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectKnowledgeLinkRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.project.repository.ProjectWizardSubmissionRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.service.QuoteService;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserService userService;
    private final SourceReferenceService sourceReferenceService;
    private final QuoteService quoteService;
    private final ActionItemService actionItemService;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectProblemRepository problemRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final DesignDecisionRepository decisionRepository;
    private final PlaytestPlanRepository planRepository;
    private final PlaytestSessionRepository sessionRepository;
    private final PlaytestFindingRepository findingRepository;
    private final ProjectKnowledgeLinkRepository knowledgeLinkRepository;
    private final ProjectLensReviewRepository lensReviewRepository;
    private final ProjectWizardSubmissionRepository wizardSubmissionRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<GameProjectResponse> listProjects(String email) {
        User user = userService.getByEmailRequired(email);
        return projectRepository.findByOwnerIdAndArchivedAtIsNullOrderByUpdatedAtDesc(user.getId()).stream()
                .map(this::toProjectResponse)
                .toList();
    }

    @Transactional
    public GameProjectResponse createProject(String email, GameProjectRequest request) {
        User user = userService.getByEmailRequired(email);
        GameProject project = new GameProject();
        project.setOwner(user);
        project.setSlug(uniqueSlug(user, request.title()));
        applyProjectRequest(project, request);
        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public GameProjectResponse getProject(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toProjectResponse(getOwnedProject(user, id));
    }

    @Transactional
    public GameProjectResponse updateProject(String email, Long id, GameProjectRequest request) {
        User user = userService.getByEmailRequired(email);
        GameProject project = getOwnedProject(user, id);
        applyProjectRequest(project, request);
        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void archiveProject(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        GameProject project = getOwnedProject(user, id);
        project.setArchivedAt(Instant.now());
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectProblemResponse> listProblems(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return problemRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toProblemResponse)
                .toList();
    }

    @Transactional
    public ProjectProblemResponse createProblem(String email, Long projectId, ProjectProblemRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectProblem problem = new ProjectProblem();
        problem.setProject(getOwnedProject(user, projectId));
        applyProblemRequest(user, problem, request);
        return toProblemResponse(problemRepository.save(problem));
    }

    @Transactional
    public ProjectProblemResponse updateProblem(String email, Long id, ProjectProblemRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectProblem problem = getOwnedProblem(user, id);
        applyProblemRequest(user, problem, request);
        return toProblemResponse(problemRepository.save(problem));
    }

    @Transactional
    public void deleteProblem(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        problemRepository.delete(getOwnedProblem(user, id));
    }

    @Transactional(readOnly = true)
    public List<ProjectApplicationResponse> listApplications(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return applicationRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ProjectApplicationResponse createApplication(String email, Long projectId, ProjectApplicationRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectApplication application = new ProjectApplication();
        application.setProject(getOwnedProject(user, projectId));
        applyApplicationRequest(user, application, request);
        return toApplicationResponse(applicationRepository.save(application));
    }

    @Transactional
    public ProjectApplicationResponse updateApplication(String email, Long id, ProjectApplicationRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectApplication application = getOwnedApplication(user, id);
        applyApplicationRequest(user, application, request);
        return toApplicationResponse(applicationRepository.save(application));
    }

    @Transactional
    public void deleteApplication(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        applicationRepository.delete(getOwnedApplication(user, id));
    }

    @Transactional(readOnly = true)
    public List<DesignDecisionResponse> listDecisions(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return decisionRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toDecisionResponse)
                .toList();
    }

    @Transactional
    public DesignDecisionResponse createDecision(String email, Long projectId, DesignDecisionRequest request) {
        User user = userService.getByEmailRequired(email);
        DesignDecision decision = new DesignDecision();
        decision.setProject(getOwnedProject(user, projectId));
        applyDecisionRequest(user, decision, request);
        return toDecisionResponse(decisionRepository.save(decision));
    }

    @Transactional
    public DesignDecisionResponse updateDecision(String email, Long id, DesignDecisionRequest request) {
        User user = userService.getByEmailRequired(email);
        DesignDecision decision = getOwnedDecision(user, id);
        applyDecisionRequest(user, decision, request);
        return toDecisionResponse(decisionRepository.save(decision));
    }

    @Transactional
    public void deleteDecision(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        decisionRepository.delete(getOwnedDecision(user, id));
    }

    @Transactional(readOnly = true)
    public List<PlaytestPlanResponse> listPlaytestPlans(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return planRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional
    public PlaytestPlanResponse createPlaytestPlan(String email, Long projectId, PlaytestPlanRequest request) {
        User user = userService.getByEmailRequired(email);
        PlaytestPlan plan = new PlaytestPlan();
        plan.setProject(getOwnedProject(user, projectId));
        applyPlanRequest(plan, request);
        return toPlanResponse(planRepository.save(plan));
    }

    @Transactional
    public PlaytestPlanResponse updatePlaytestPlan(String email, Long id, PlaytestPlanRequest request) {
        User user = userService.getByEmailRequired(email);
        PlaytestPlan plan = getOwnedPlan(user, id);
        applyPlanRequest(plan, request);
        return toPlanResponse(planRepository.save(plan));
    }

    @Transactional
    public void deletePlaytestPlan(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        planRepository.delete(getOwnedPlan(user, id));
    }

    @Transactional(readOnly = true)
    public List<PlaytestFindingResponse> listPlaytestFindings(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return findingRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toFindingResponse)
                .toList();
    }

    @Transactional
    public PlaytestFindingResponse createPlaytestFinding(String email, Long projectId, PlaytestFindingRequest request) {
        User user = userService.getByEmailRequired(email);
        PlaytestFinding finding = new PlaytestFinding();
        finding.setProject(getOwnedProject(user, projectId));
        applyFindingRequest(user, finding, request);
        return toFindingResponse(findingRepository.save(finding));
    }

    @Transactional
    public PlaytestFindingResponse updatePlaytestFinding(String email, Long id, PlaytestFindingRequest request) {
        User user = userService.getByEmailRequired(email);
        PlaytestFinding finding = getOwnedFinding(user, id);
        applyFindingRequest(user, finding, request);
        return toFindingResponse(findingRepository.save(finding));
    }

    @Transactional
    public void deletePlaytestFinding(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        findingRepository.delete(getOwnedFinding(user, id));
    }

    @Transactional(readOnly = true)
    public List<ProjectKnowledgeLinkResponse> listKnowledgeLinks(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return knowledgeLinkRepository.findByProjectIdAndProjectOwnerIdOrderByCreatedAtDesc(projectId, user.getId()).stream()
                .map(this::toKnowledgeLinkResponse)
                .toList();
    }

    @Transactional
    public ProjectKnowledgeLinkResponse createKnowledgeLink(String email, Long projectId, ProjectKnowledgeLinkRequest request) {
        User user = userService.getByEmailRequired(email);
        validateTarget(user, request.targetType(), request.targetId());
        GameProject project = getOwnedProject(user, projectId);
        String targetType = normalizeType(request.targetType());
        String relationshipType = defaultText(request.relationshipType(), "APPLIES_TO");
        ProjectKnowledgeLink link = knowledgeLinkRepository
                .findByProjectIdAndTargetTypeAndTargetIdAndRelationshipType(project.getId(), targetType, request.targetId(), relationshipType)
                .orElseGet(ProjectKnowledgeLink::new);
        link.setProject(project);
        link.setTargetType(targetType);
        link.setTargetId(request.targetId());
        link.setRelationshipType(relationshipType);
        link.setNote(trimToNull(request.note()));
        link.setSourceReference(ownedSourceOrNull(user, request.sourceReferenceId()));
        return toKnowledgeLinkResponse(knowledgeLinkRepository.save(link));
    }

    @Transactional
    public void deleteKnowledgeLink(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        knowledgeLinkRepository.delete(knowledgeLinkRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project knowledge link not found.")));
    }

    @Transactional(readOnly = true)
    public List<ProjectLensReviewResponse> listLensReviews(String email, Long projectId) {
        User user = userService.getByEmailRequired(email);
        getOwnedProject(user, projectId);
        return lensReviewRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(projectId, user.getId()).stream()
                .map(this::toLensReviewResponse)
                .toList();
    }

    @Transactional
    public ProjectLensReviewResponse createLensReview(String email, Long projectId, ProjectLensReviewRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectLensReview review = new ProjectLensReview();
        review.setProject(getOwnedProject(user, projectId));
        applyLensReviewRequest(user, review, request);
        return toLensReviewResponse(lensReviewRepository.save(review));
    }

    @Transactional
    public ProjectLensReviewResponse updateLensReview(String email, Long id, ProjectLensReviewRequest request) {
        User user = userService.getByEmailRequired(email);
        ProjectLensReview review = getOwnedLensReview(user, id);
        applyLensReviewRequest(user, review, request);
        return toLensReviewResponse(lensReviewRepository.save(review));
    }

    @Transactional
    public void deleteLensReview(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        lensReviewRepository.delete(getOwnedLensReview(user, id));
    }

    @Transactional
    public ProjectApplicationResponse applySourceReference(String email, Long projectId, ApplySourceRequest request) {
        User user = userService.getByEmailRequired(email);
        SourceReference source = sourceReferenceService.getOwnedSourceReference(user, request.sourceId());
        return createDerivedApplication(
                user,
                getOwnedProject(user, projectId),
                "SOURCE_REFERENCE",
                source.getId(),
                source,
                defaultText(request.applicationType(), "SOURCE_APPLICATION"),
                defaultText(request.title(), "Apply source reference"),
                request.description());
    }

    @Transactional
    public ProjectApplicationResponse applyQuote(String email, Long projectId, ApplySourceRequest request) {
        User user = userService.getByEmailRequired(email);
        Quote quote = quoteService.getOwnedQuote(user, request.sourceId());
        SourceReference source = ownedSourceOrNull(user, quote.getSourceReferenceId());
        return createDerivedApplication(
                user,
                getOwnedProject(user, projectId),
                "QUOTE",
                quote.getId(),
                source,
                defaultText(request.applicationType(), "QUOTE_APPLICATION"),
                defaultText(request.title(), "Apply quote"),
                defaultText(request.description(), quote.getText()));
    }

    @Transactional
    public ProjectApplicationResponse applyConcept(String email, Long projectId, ApplySourceRequest request) {
        User user = userService.getByEmailRequired(email);
        Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(request.sourceId(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Concept not found."));
        return createDerivedApplication(
                user,
                getOwnedProject(user, projectId),
                "CONCEPT",
                concept.getId(),
                concept.getFirstSourceReference(),
                defaultText(request.applicationType(), "CONCEPT_APPLICATION"),
                defaultText(request.title(), "Apply concept: " + concept.getName()),
                defaultText(request.description(), concept.getDescription()));
    }

    @Transactional
    public ProjectApplicationResponse applyKnowledgeObject(String email, Long projectId, ApplySourceRequest request) {
        User user = userService.getByEmailRequired(email);
        KnowledgeObject object = knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(request.sourceId(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
        return createDerivedApplication(
                user,
                getOwnedProject(user, projectId),
                "KNOWLEDGE_OBJECT",
                object.getId(),
                ownedSourceOrNull(user, object.getSourceReferenceId()),
                defaultText(request.applicationType(), "KNOWLEDGE_APPLICATION"),
                defaultText(request.title(), "Apply knowledge: " + object.getTitle()),
                defaultText(request.description(), object.getDescription()));
    }

    @Transactional
    public ProjectApplicationResponse createPrototypeTaskFromDaily(
            String email,
            Long projectId,
            ProjectPrototypeTaskFromDailyRequest request) {
        User user = userService.getByEmailRequired(email);
        DailyDesignPrompt prompt = dailyDesignPromptRepository.findByIdAndUserId(request.dailyDesignPromptId(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));
        return createDerivedApplication(
                user,
                getOwnedProject(user, projectId),
                "DAILY_DESIGN_PROMPT",
                prompt.getId(),
                ownedSourceOrNull(user, prompt.getSourceReferenceId()),
                "PROTOTYPE_TASK",
                defaultText(request.title(), "Prototype task from daily prompt"),
                defaultText(request.description(), prompt.getQuestion()));
    }

    @Transactional
    public ProjectApplyKnowledgeWizardResponse applyKnowledgeWizard(
            String email,
            Long projectId,
            ProjectApplyKnowledgeWizardRequest request) {
        User user = userService.getByEmailRequired(email);
        GameProject project = getOwnedProject(user, projectId);
        String idempotencyKey = request.idempotencyKey().trim();

        return wizardSubmissionRepository
                .findByProjectIdAndOwnerIdAndIdempotencyKey(project.getId(), user.getId(), idempotencyKey)
                .map(this::cachedWizardResponse)
                .orElseGet(() -> createWizardRecords(user, project, request, idempotencyKey));
    }

    private ProjectApplyKnowledgeWizardResponse createWizardRecords(
            User user,
            GameProject project,
            ProjectApplyKnowledgeWizardRequest request,
            String idempotencyKey) {
        WizardSource source = resolveWizardSource(user, request);
        SourceReference sourceReference = resolveWizardSourceReference(user, request, source);
        validateWizardPayload(user, project, request);

        List<ProjectWizardCreatedRecordResponse> createdRecords = new ArrayList<>();
        ProjectProblemResponse problemResponse = null;
        ProjectApplicationResponse applicationResponse = null;
        DesignDecisionResponse decisionResponse = null;
        PlaytestPlanResponse planResponse = null;
        PlaytestFindingResponse findingResponse = null;
        ProjectLensReviewResponse lensReviewResponse = null;
        ProjectKnowledgeLinkResponse knowledgeLinkResponse = null;

        if (request.projectProblem() != null) {
            ProjectProblem problem = new ProjectProblem();
            problem.setProject(project);
            applyProblemRequest(user, problem, withSource(request.projectProblem(), sourceReference));
            problemResponse = toProblemResponse(problemRepository.save(problem));
            createdRecords.add(createdRecord("PROJECT_PROBLEM", problemResponse.id(), problemResponse.title(), problemResponse.relatedSourceReference() != null));
        }

        if (request.projectApplication() != null) {
            ProjectApplication application = new ProjectApplication();
            application.setProject(project);
            applyApplicationRequest(user, application, withSource(request.projectApplication(), source, sourceReference));
            applicationResponse = toApplicationResponse(applicationRepository.save(application));
            createdRecords.add(createdRecord("PROJECT_APPLICATION", applicationResponse.id(), applicationResponse.title(), applicationResponse.sourceReference() != null));
            createOrUpdateKnowledgeLink(project, application.getSourceEntityType(), application.getSourceEntityId(), "APPLIES_TO", null, application.getSourceReference());
        }

        if (request.designDecision() != null) {
            DesignDecision decision = new DesignDecision();
            decision.setProject(project);
            applyDecisionRequest(user, decision, withSource(request.designDecision(), sourceReference));
            decisionResponse = toDecisionResponse(decisionRepository.save(decision));
            createdRecords.add(createdRecord("DESIGN_DECISION", decisionResponse.id(), decisionResponse.title(), decisionResponse.sourceReference() != null));
        }

        if (request.playtestPlan() != null) {
            PlaytestPlan plan = new PlaytestPlan();
            plan.setProject(project);
            applyPlanRequest(plan, request.playtestPlan());
            planResponse = toPlanResponse(planRepository.save(plan));
            createdRecords.add(createdRecord("PLAYTEST_PLAN", planResponse.id(), planResponse.title(), false));
        }

        if (request.playtestFinding() != null) {
            PlaytestFinding finding = new PlaytestFinding();
            finding.setProject(project);
            applyFindingRequest(user, finding, withSource(request.playtestFinding(), sourceReference));
            findingResponse = toFindingResponse(findingRepository.save(finding));
            createdRecords.add(createdRecord("PLAYTEST_FINDING", findingResponse.id(), findingResponse.title(), findingResponse.sourceReference() != null));
        }

        if (request.lensReview() != null) {
            ProjectLensReview review = new ProjectLensReview();
            review.setProject(project);
            applyLensReviewRequest(user, review, withSource(request.lensReview(), sourceReference));
            lensReviewResponse = toLensReviewResponse(lensReviewRepository.save(review));
            createdRecords.add(createdRecord("PROJECT_LENS_REVIEW", lensReviewResponse.id(), lensReviewResponse.question(), lensReviewResponse.sourceReference() != null));
        }

        if (request.projectKnowledgeLink() != null) {
            ProjectKnowledgeLink link = createOrUpdateKnowledgeLink(
                    project,
                    normalizeType(request.projectKnowledgeLink().targetType()),
                    request.projectKnowledgeLink().targetId(),
                    defaultText(request.projectKnowledgeLink().relationshipType(), "APPLIES_TO"),
                    request.projectKnowledgeLink().note(),
                    sourceReferenceOrPayload(user, request.projectKnowledgeLink().sourceReferenceId(), sourceReference));
            knowledgeLinkResponse = toKnowledgeLinkResponse(link);
            createdRecords.add(createdRecord("PROJECT_KNOWLEDGE_LINK", knowledgeLinkResponse.id(), knowledgeLinkResponse.note(), knowledgeLinkResponse.sourceReference() != null));
        }

        if (createdRecords.isEmpty()) {
            throw new IllegalArgumentException("Wizard request must include at least one record to create.");
        }

        ProjectApplyKnowledgeWizardResponse response = new ProjectApplyKnowledgeWizardResponse(
                project.getId(),
                idempotencyKey,
                false,
                trimToNull(request.clientStepIntent()),
                problemResponse,
                applicationResponse,
                decisionResponse,
                planResponse,
                findingResponse,
                lensReviewResponse,
                knowledgeLinkResponse,
                createdRecords);

        ProjectWizardSubmission submission = new ProjectWizardSubmission();
        submission.setOwner(user);
        submission.setProject(project);
        submission.setIdempotencyKey(idempotencyKey);
        submission.setClientStepIntent(trimToNull(request.clientStepIntent()));
        submission.setResponseJson(writeWizardResponse(response));
        wizardSubmissionRepository.save(submission);
        return response;
    }

    private ProjectApplyKnowledgeWizardResponse cachedWizardResponse(ProjectWizardSubmission submission) {
        try {
            return objectMapper.readValue(submission.getResponseJson(), ProjectApplyKnowledgeWizardResponse.class).asDuplicate();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored project wizard response is unreadable.", exception);
        }
    }

    private String writeWizardResponse(ProjectApplyKnowledgeWizardResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Project wizard response could not be stored.", exception);
        }
    }

    private ProjectWizardCreatedRecordResponse createdRecord(String type, Long id, String title, boolean sourcePreserved) {
        return new ProjectWizardCreatedRecordResponse(type, id, defaultText(title, type), sourcePreserved);
    }

    private void validateWizardPayload(User user, GameProject project, ProjectApplyKnowledgeWizardRequest request) {
        sourceReferenceOrPayload(user, request.sourceReferenceId(), null);
        validateSourceReferenceId(user, request.projectProblem() == null ? null : request.projectProblem().relatedSourceReferenceId());
        validateApplicationRequest(user, request.projectApplication());
        validateSourceReferenceId(user, request.designDecision() == null ? null : request.designDecision().sourceReferenceId());
        validateFindingRequest(user, project, request.playtestFinding());
        validateLensReviewRequest(user, request.lensReview());
        validateKnowledgeLinkRequest(user, request.projectKnowledgeLink());
    }

    private void validateApplicationRequest(User user, ProjectApplicationRequest request) {
        if (request == null) {
            return;
        }
        if (StringUtils.hasText(request.sourceEntityType()) != (request.sourceEntityId() != null)) {
            throw new IllegalArgumentException("Application source entity type and id must be supplied together.");
        }
        if (StringUtils.hasText(request.sourceEntityType())) {
            validateTarget(user, request.sourceEntityType(), request.sourceEntityId());
        }
        validateSourceReferenceId(user, request.sourceReferenceId());
    }

    private void validateFindingRequest(User user, GameProject project, PlaytestFindingRequest request) {
        if (request == null) {
            return;
        }
        validateSourceReferenceId(user, request.sourceReferenceId());
        if (request.sessionId() != null) {
            PlaytestSession session = sessionRepository.findByIdAndProjectOwnerId(request.sessionId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Playtest session not found."));
            if (!Objects.equals(session.getProject().getId(), project.getId())) {
                throw new IllegalArgumentException("Playtest session belongs to a different project.");
            }
        }
    }

    private void validateLensReviewRequest(User user, ProjectLensReviewRequest request) {
        if (request == null) {
            return;
        }
        validateSourceReferenceId(user, request.sourceReferenceId());
        if (request.knowledgeObjectId() != null) {
            knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(request.knowledgeObjectId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
        }
    }

    private void validateKnowledgeLinkRequest(User user, ProjectKnowledgeLinkRequest request) {
        if (request == null) {
            return;
        }
        validateTarget(user, request.targetType(), request.targetId());
        validateSourceReferenceId(user, request.sourceReferenceId());
    }

    private void validateSourceReferenceId(User user, Long sourceReferenceId) {
        if (sourceReferenceId != null) {
            sourceReferenceService.getOwnedSourceReference(user, sourceReferenceId);
        }
    }

    private WizardSource resolveWizardSource(User user, ProjectApplyKnowledgeWizardRequest request) {
        String sourceType = normalizeTypeOrNull(request.sourceType());
        Long sourceId = request.sourceId();
        if (!StringUtils.hasText(sourceType) && sourceId == null && request.sourceReferenceId() != null) {
            sourceType = "SOURCE_REFERENCE";
            sourceId = request.sourceReferenceId();
        }
        if (!StringUtils.hasText(sourceType) && sourceId == null) {
            return null;
        }
        if (!StringUtils.hasText(sourceType) || sourceId == null) {
            throw new IllegalArgumentException("Wizard source type and id must be supplied together.");
        }

        return switch (sourceType) {
            case "QUOTE" -> {
                Quote quote = quoteService.getOwnedQuote(user, sourceId);
                yield new WizardSource("QUOTE", quote.getId(), ownedSourceOrNull(user, quote.getSourceReferenceId()), "Apply quote", quote.getText());
            }
            case "CONCEPT" -> {
                Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(sourceId, user.getId())
                        .orElseThrow(() -> new NoSuchElementException("Concept not found."));
                yield new WizardSource("CONCEPT", concept.getId(), concept.getFirstSourceReference(), "Apply concept: " + concept.getName(), concept.getDescription());
            }
            case "KNOWLEDGE_OBJECT" -> {
                KnowledgeObject object = knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(sourceId, user.getId())
                        .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
                yield new WizardSource("KNOWLEDGE_OBJECT", object.getId(), ownedSourceOrNull(user, object.getSourceReferenceId()), "Apply knowledge: " + object.getTitle(), object.getDescription());
            }
            case "SOURCE_REFERENCE" -> {
                SourceReference sourceReference = sourceReferenceService.getOwnedSourceReference(user, sourceId);
                yield new WizardSource("SOURCE_REFERENCE", sourceReference.getId(), sourceReference, "Apply source reference", sourceReference.getSourceText());
            }
            case "DAILY_PROMPT", "DAILY_DESIGN_PROMPT" -> {
                DailyDesignPrompt prompt = dailyDesignPromptRepository.findByIdAndUserId(sourceId, user.getId())
                        .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));
                yield new WizardSource("DAILY_DESIGN_PROMPT", prompt.getId(), ownedSourceOrNull(user, prompt.getSourceReferenceId()), "Prototype task from daily prompt", prompt.getQuestion());
            }
            default -> throw new IllegalArgumentException("Unsupported wizard source type: " + sourceType);
        };
    }

    private SourceReference resolveWizardSourceReference(User user, ProjectApplyKnowledgeWizardRequest request, WizardSource source) {
        SourceReference explicitSourceReference = sourceReferenceOrPayload(user, request.sourceReferenceId(), null);
        if (explicitSourceReference != null) {
            return explicitSourceReference;
        }
        return source == null ? null : source.sourceReference();
    }

    private ProjectProblemRequest withSource(ProjectProblemRequest request, SourceReference sourceReference) {
        return new ProjectProblemRequest(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.relatedSourceReferenceId() == null ? sourceReferenceId(sourceReference) : request.relatedSourceReferenceId());
    }

    private ProjectApplicationRequest withSource(ProjectApplicationRequest request, WizardSource source, SourceReference sourceReference) {
        return new ProjectApplicationRequest(
                StringUtils.hasText(request.sourceEntityType()) ? request.sourceEntityType() : source == null ? null : source.type(),
                request.sourceEntityId() == null && source != null ? source.id() : request.sourceEntityId(),
                request.sourceReferenceId() == null ? sourceReferenceId(sourceReference) : request.sourceReferenceId(),
                defaultText(request.applicationType(), source == null ? "PROJECT_APPLICATION" : defaultApplicationType(source.type())),
                request.title(),
                defaultText(request.description(), source == null ? null : source.fallbackDescription()),
                request.status());
    }

    private DesignDecisionRequest withSource(DesignDecisionRequest request, SourceReference sourceReference) {
        return new DesignDecisionRequest(
                request.title(),
                request.decision(),
                request.rationale(),
                request.tradeoffs(),
                request.sourceReferenceId() == null ? sourceReferenceId(sourceReference) : request.sourceReferenceId(),
                request.status());
    }

    private PlaytestFindingRequest withSource(PlaytestFindingRequest request, SourceReference sourceReference) {
        return new PlaytestFindingRequest(
                request.sessionId(),
                request.title(),
                request.observation(),
                request.severity(),
                request.recommendation(),
                request.sourceReferenceId() == null ? sourceReferenceId(sourceReference) : request.sourceReferenceId(),
                request.status());
    }

    private ProjectLensReviewRequest withSource(ProjectLensReviewRequest request, SourceReference sourceReference) {
        return new ProjectLensReviewRequest(
                request.knowledgeObjectId(),
                request.question(),
                request.answer(),
                request.score(),
                request.status(),
                request.sourceReferenceId() == null ? sourceReferenceId(sourceReference) : request.sourceReferenceId());
    }

    private ProjectKnowledgeLink createOrUpdateKnowledgeLink(
            GameProject project,
            String targetType,
            Long targetId,
            String relationshipType,
            String note,
            SourceReference sourceReference) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            return null;
        }
        String normalizedTargetType = normalizeType(targetType);
        String normalizedRelationship = defaultText(relationshipType, "APPLIES_TO");
        ProjectKnowledgeLink link = knowledgeLinkRepository
                .findByProjectIdAndTargetTypeAndTargetIdAndRelationshipType(project.getId(), normalizedTargetType, targetId, normalizedRelationship)
                .orElseGet(ProjectKnowledgeLink::new);
        link.setProject(project);
        link.setTargetType(normalizedTargetType);
        link.setTargetId(targetId);
        link.setRelationshipType(normalizedRelationship);
        link.setNote(trimToNull(note));
        link.setSourceReference(sourceReference);
        return knowledgeLinkRepository.save(link);
    }

    private SourceReference sourceReferenceOrPayload(User user, Long payloadSourceReferenceId, SourceReference defaultSourceReference) {
        return payloadSourceReferenceId == null ? defaultSourceReference : sourceReferenceService.getOwnedSourceReference(user, payloadSourceReferenceId);
    }

    private Long sourceReferenceId(SourceReference sourceReference) {
        return sourceReference == null ? null : sourceReference.getId();
    }

    private String defaultApplicationType(String sourceType) {
        return switch (normalizeType(sourceType)) {
            case "QUOTE" -> "QUOTE_APPLICATION";
            case "CONCEPT" -> "CONCEPT_APPLICATION";
            case "KNOWLEDGE_OBJECT" -> "KNOWLEDGE_APPLICATION";
            case "SOURCE_REFERENCE" -> "SOURCE_APPLICATION";
            case "DAILY_PROMPT", "DAILY_DESIGN_PROMPT" -> "PROTOTYPE_TASK";
            default -> "PROJECT_APPLICATION";
        };
    }

    private record WizardSource(
            String type,
            Long id,
            SourceReference sourceReference,
            String fallbackTitle,
            String fallbackDescription) {}

    private ProjectApplicationResponse createDerivedApplication(
            User user,
            GameProject project,
            String sourceEntityType,
            Long sourceEntityId,
            SourceReference sourceReference,
            String applicationType,
            String title,
            String description) {
        ProjectApplication application = new ProjectApplication();
        application.setProject(project);
        application.setSourceEntityType(sourceEntityType);
        application.setSourceEntityId(sourceEntityId);
        application.setSourceReference(sourceReference);
        application.setApplicationType(applicationType);
        application.setTitle(title);
        application.setDescription(trimToNull(description));
        application.setStatus("OPEN");
        ProjectApplication saved = applicationRepository.save(application);

        ProjectKnowledgeLink link = knowledgeLinkRepository
                .findByProjectIdAndTargetTypeAndTargetIdAndRelationshipType(project.getId(), sourceEntityType, sourceEntityId, "APPLIES_TO")
                .orElseGet(ProjectKnowledgeLink::new);
        link.setProject(project);
        link.setTargetType(sourceEntityType);
        link.setTargetId(sourceEntityId);
        link.setRelationshipType("APPLIES_TO");
        link.setSourceReference(sourceReference);
        knowledgeLinkRepository.save(link);

        return toApplicationResponse(saved);
    }

    private GameProject getOwnedProject(User user, Long id) {
        return projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project not found."));
    }

    private ProjectProblem getOwnedProblem(User user, Long id) {
        return problemRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project problem not found."));
    }

    private ProjectApplication getOwnedApplication(User user, Long id) {
        return applicationRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project application not found."));
    }

    private DesignDecision getOwnedDecision(User user, Long id) {
        return decisionRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Design decision not found."));
    }

    private PlaytestPlan getOwnedPlan(User user, Long id) {
        return planRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Playtest plan not found."));
    }

    private PlaytestFinding getOwnedFinding(User user, Long id) {
        return findingRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Playtest finding not found."));
    }

    private ProjectLensReview getOwnedLensReview(User user, Long id) {
        return lensReviewRepository.findByIdAndProjectOwnerId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project lens review not found."));
    }

    private void applyProjectRequest(GameProject project, GameProjectRequest request) {
        project.setTitle(request.title().trim());
        project.setDescription(trimToNull(request.description()));
        project.setGenre(trimToNull(request.genre()));
        project.setPlatform(trimToNull(request.platform()));
        project.setStage(defaultText(request.stage(), project.getStage()));
        project.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());
        project.setProgressPercent(request.progressPercent() == null ? project.getProgressPercent() : request.progressPercent());
    }

    private void applyProblemRequest(User user, ProjectProblem problem, ProjectProblemRequest request) {
        problem.setTitle(request.title().trim());
        problem.setDescription(trimToNull(request.description()));
        problem.setStatus(defaultText(request.status(), problem.getStatus()));
        problem.setPriority(defaultText(request.priority(), problem.getPriority()));
        problem.setRelatedSourceReference(ownedSourceOrNull(user, request.relatedSourceReferenceId()));
    }

    private void applyApplicationRequest(User user, ProjectApplication application, ProjectApplicationRequest request) {
        if (StringUtils.hasText(request.sourceEntityType()) && request.sourceEntityId() != null) {
            validateTarget(user, request.sourceEntityType(), request.sourceEntityId());
        }
        application.setSourceEntityType(normalizeTypeOrNull(request.sourceEntityType()));
        application.setSourceEntityId(request.sourceEntityId());
        application.setSourceReference(ownedSourceOrNull(user, request.sourceReferenceId()));
        application.setApplicationType(defaultText(request.applicationType(), application.getApplicationType()));
        application.setTitle(request.title().trim());
        application.setDescription(trimToNull(request.description()));
        application.setStatus(defaultText(request.status(), application.getStatus()));
    }

    private void applyDecisionRequest(User user, DesignDecision decision, DesignDecisionRequest request) {
        decision.setTitle(request.title().trim());
        decision.setDecision(request.decision().trim());
        decision.setRationale(trimToNull(request.rationale()));
        decision.setTradeoffs(trimToNull(request.tradeoffs()));
        decision.setSourceReference(ownedSourceOrNull(user, request.sourceReferenceId()));
        decision.setStatus(defaultText(request.status(), decision.getStatus()));
    }

    private void applyPlanRequest(PlaytestPlan plan, PlaytestPlanRequest request) {
        plan.setTitle(request.title().trim());
        plan.setHypothesis(trimToNull(request.hypothesis()));
        plan.setTargetPlayers(trimToNull(request.targetPlayers()));
        plan.setTasks(trimToNull(request.tasks()));
        plan.setSuccessCriteria(trimToNull(request.successCriteria()));
        plan.setStatus(defaultText(request.status(), plan.getStatus()));
    }

    private void applyFindingRequest(User user, PlaytestFinding finding, PlaytestFindingRequest request) {
        finding.setSession(request.sessionId() == null ? null : sessionRepository.findByIdAndProjectOwnerId(request.sessionId(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Playtest session not found.")));
        if (finding.getSession() != null && !Objects.equals(finding.getSession().getProject().getId(), finding.getProject().getId())) {
            throw new IllegalArgumentException("Playtest session belongs to a different project.");
        }
        finding.setTitle(request.title().trim());
        finding.setObservation(trimToNull(request.observation()));
        finding.setSeverity(defaultText(request.severity(), finding.getSeverity()));
        finding.setRecommendation(trimToNull(request.recommendation()));
        finding.setSourceReference(ownedSourceOrNull(user, request.sourceReferenceId()));
        finding.setStatus(defaultText(request.status(), finding.getStatus()));
    }

    private void applyLensReviewRequest(User user, ProjectLensReview review, ProjectLensReviewRequest request) {
        KnowledgeObject object = request.knowledgeObjectId() == null ? null : knowledgeObjectRepository
                .findByIdAndUserIdAndArchivedFalse(request.knowledgeObjectId(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
        review.setKnowledgeObject(object);
        review.setQuestion(request.question().trim());
        review.setAnswer(trimToNull(request.answer()));
        review.setScore(request.score());
        review.setStatus(defaultText(request.status(), review.getStatus()));
        review.setSourceReference(ownedSourceOrNull(user, request.sourceReferenceId()));
    }

    private void validateTarget(User user, String targetType, Long targetId) {
        switch (normalizeType(targetType)) {
            case "QUOTE" -> quoteService.getOwnedQuote(user, targetId);
            case "ACTION_ITEM" -> actionItemService.getOwnedActionItem(user, targetId);
            case "CONCEPT" -> conceptRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Concept not found."));
            case "KNOWLEDGE_OBJECT" -> knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
            case "SOURCE_REFERENCE" -> sourceReferenceService.getOwnedSourceReference(user, targetId);
            case "FORUM_THREAD" -> {
                ForumThread thread = forumThreadRepository.findByIdAndStatusNot(targetId, ForumThreadStatus.ARCHIVED)
                        .orElseThrow(() -> new NoSuchElementException("Forum thread not found."));
                if (!Objects.equals(thread.getAuthor().getId(), user.getId())) {
                    throw new NoSuchElementException("Forum thread not found.");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported project knowledge target type: " + targetType);
        }
    }

    private SourceReference ownedSourceOrNull(User user, Long sourceReferenceId) {
        return sourceReferenceId == null ? null : sourceReferenceService.getOwnedSourceReference(user, sourceReferenceId);
    }

    private SourceReferenceResponse sourceResponse(SourceReference sourceReference) {
        return sourceReference == null ? null : sourceReferenceService.toResponse(sourceReference);
    }

    private GameProjectResponse toProjectResponse(GameProject project) {
        return new GameProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getSlug(),
                project.getDescription(),
                project.getGenre(),
                project.getPlatform(),
                project.getStage(),
                project.getVisibility(),
                project.getProgressPercent(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getArchivedAt());
    }

    private ProjectProblemResponse toProblemResponse(ProjectProblem problem) {
        return new ProjectProblemResponse(
                problem.getId(),
                problem.getProject().getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getStatus(),
                problem.getPriority(),
                sourceResponse(problem.getRelatedSourceReference()),
                problem.getCreatedAt(),
                problem.getUpdatedAt());
    }

    private ProjectApplicationResponse toApplicationResponse(ProjectApplication application) {
        return new ProjectApplicationResponse(
                application.getId(),
                application.getProject().getId(),
                application.getSourceEntityType(),
                application.getSourceEntityId(),
                sourceResponse(application.getSourceReference()),
                application.getApplicationType(),
                application.getTitle(),
                application.getDescription(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    private DesignDecisionResponse toDecisionResponse(DesignDecision decision) {
        return new DesignDecisionResponse(
                decision.getId(),
                decision.getProject().getId(),
                decision.getTitle(),
                decision.getDecision(),
                decision.getRationale(),
                decision.getTradeoffs(),
                sourceResponse(decision.getSourceReference()),
                decision.getStatus(),
                decision.getCreatedAt(),
                decision.getUpdatedAt());
    }

    private PlaytestPlanResponse toPlanResponse(PlaytestPlan plan) {
        return new PlaytestPlanResponse(
                plan.getId(),
                plan.getProject().getId(),
                plan.getTitle(),
                plan.getHypothesis(),
                plan.getTargetPlayers(),
                plan.getTasks(),
                plan.getSuccessCriteria(),
                plan.getStatus(),
                plan.getCreatedAt(),
                plan.getUpdatedAt());
    }

    private PlaytestFindingResponse toFindingResponse(PlaytestFinding finding) {
        return new PlaytestFindingResponse(
                finding.getId(),
                finding.getProject().getId(),
                finding.getSession() == null ? null : finding.getSession().getId(),
                finding.getTitle(),
                finding.getObservation(),
                finding.getSeverity(),
                finding.getRecommendation(),
                sourceResponse(finding.getSourceReference()),
                finding.getStatus(),
                finding.getCreatedAt(),
                finding.getUpdatedAt());
    }

    private ProjectKnowledgeLinkResponse toKnowledgeLinkResponse(ProjectKnowledgeLink link) {
        return new ProjectKnowledgeLinkResponse(
                link.getId(),
                link.getProject().getId(),
                link.getTargetType(),
                link.getTargetId(),
                link.getRelationshipType(),
                link.getNote(),
                sourceResponse(link.getSourceReference()),
                link.getCreatedAt());
    }

    private ProjectLensReviewResponse toLensReviewResponse(ProjectLensReview review) {
        return new ProjectLensReviewResponse(
                review.getId(),
                review.getProject().getId(),
                review.getKnowledgeObject() == null ? null : review.getKnowledgeObject().getId(),
                review.getKnowledgeObject() == null ? null : review.getKnowledgeObject().getTitle(),
                review.getQuestion(),
                review.getAnswer(),
                review.getScore(),
                review.getStatus(),
                sourceResponse(review.getSourceReference()),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private String uniqueSlug(User user, String title) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 2;
        while (projectRepository.existsByOwnerIdAndSlug(user.getId(), candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return StringUtils.hasText(normalized) ? normalized : "project";
    }

    private String normalizeType(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeTypeOrNull(String value) {
        return StringUtils.hasText(value) ? normalizeType(value) : null;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
