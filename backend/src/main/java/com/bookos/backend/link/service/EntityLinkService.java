package com.bookos.backend.link.service;

import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.daily.repository.DailySentenceRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.dto.EntityLinkRequest;
import com.bookos.backend.link.dto.EntityLinkResponse;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.note.repository.NoteBlockRepository;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EntityLinkService {

    private final EntityLinkRepository entityLinkRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final BookNoteRepository bookNoteRepository;
    private final NoteBlockRepository noteBlockRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final UserBookRepository userBookRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final DailySentenceRepository dailySentenceRepository;
    private final GameProjectRepository gameProjectRepository;
    private final ProjectProblemRepository projectProblemRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DesignDecisionRepository designDecisionRepository;
    private final PlaytestFindingRepository playtestFindingRepository;
    private final ProjectLensReviewRepository projectLensReviewRepository;
    private final UserService userService;

    private static final String CREATED_BY_USER = "USER";
    private static final String CREATED_BY_SYSTEM = "SYSTEM";

    @Transactional(readOnly = true)
    public List<EntityLinkResponse> listEntityLinks(String email, String sourceType, Long sourceId, String targetType, Long targetId) {
        User user = userService.getByEmailRequired(email);
        if (StringUtils.hasText(sourceType) && sourceId != null) {
            return entityLinkRepository.findByUserIdAndSourceTypeAndSourceIdOrderByCreatedAtDesc(
                            user.getId(), normalize(sourceType), sourceId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (StringUtils.hasText(targetType) && targetId != null) {
            return entityLinkRepository.findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(
                            user.getId(), normalize(targetType), targetId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        return entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EntityLinkResponse createEntityLink(String email, EntityLinkRequest request) {
        User user = userService.getByEmailRequired(email);
        String sourceType = normalize(request.sourceType());
        String targetType = normalize(request.targetType());
        String relationType = normalize(request.relationType());

        assertOwnedOrLinkable(user, sourceType, request.sourceId());
        assertOwnedOrLinkable(user, targetType, request.targetId());
        if (request.sourceReferenceId() != null) {
            sourceReferenceRepository.findByIdAndUserId(request.sourceReferenceId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
        }

        EntityLink link = entityLinkRepository.findByUserIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetIdAndRelationType(
                        user.getId(), sourceType, request.sourceId(), targetType, request.targetId(), relationType)
                .orElseGet(() -> {
                    EntityLink created = new EntityLink();
                    created.setUser(user);
                    created.setSourceType(sourceType);
                    created.setSourceId(request.sourceId());
                    created.setTargetType(targetType);
                    created.setTargetId(request.targetId());
                    created.setRelationType(relationType);
                    created.setCreatedBy(CREATED_BY_USER);
                    return created;
                });
        link.setSourceReferenceId(request.sourceReferenceId());
        link.setNote(trimToNull(request.note()));
        return toResponse(entityLinkRepository.save(link));
    }

    @Transactional
    public EntityLinkResponse updateEntityLink(String email, Long id, EntityLinkRequest request) {
        User user = userService.getByEmailRequired(email);
        EntityLink link = entityLinkRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Entity link not found."));
        assertUserCreated(link);

        String sourceType = normalize(request.sourceType());
        String targetType = normalize(request.targetType());
        String relationType = normalize(request.relationType());
        assertOwnedOrLinkable(user, sourceType, request.sourceId());
        assertOwnedOrLinkable(user, targetType, request.targetId());
        if (request.sourceReferenceId() != null) {
            sourceReferenceRepository.findByIdAndUserId(request.sourceReferenceId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
        }

        link.setSourceType(sourceType);
        link.setSourceId(request.sourceId());
        link.setTargetType(targetType);
        link.setTargetId(request.targetId());
        link.setRelationType(relationType);
        link.setSourceReferenceId(request.sourceReferenceId());
        link.setNote(trimToNull(request.note()));
        link.setCreatedBy(CREATED_BY_USER);
        return toResponse(entityLinkRepository.save(link));
    }

    @Transactional
    public void deleteEntityLink(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        EntityLink link = entityLinkRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Entity link not found."));
        assertUserCreated(link);
        entityLinkRepository.delete(link);
    }

    private void assertOwnedOrLinkable(User user, String type, Long id) {
        switch (type) {
            case "SOURCE_REFERENCE" -> sourceReferenceRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
            case "CONCEPT" -> conceptRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Concept not found."));
            case "KNOWLEDGE_OBJECT" -> knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
            case "NOTE" -> bookNoteRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Note not found."));
            case "NOTE_BLOCK" -> noteBlockRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Note block not found."));
            case "RAW_CAPTURE" -> rawCaptureRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Capture not found."));
            case "CAPTURE" -> rawCaptureRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Capture not found."));
            case "QUOTE" -> quoteRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Quote not found."));
            case "ACTION_ITEM" -> actionItemRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Action item not found."));
            case "FORUM_THREAD" -> forumThreadRepository.findByIdAndStatusNot(id, ForumThreadStatus.ARCHIVED)
                    .filter(thread -> thread.getAuthor().getId().equals(user.getId()))
                    .orElseThrow(() -> new NoSuchElementException("Forum thread not found."));
            case "DAILY_PROMPT", "DAILY_DESIGN_PROMPT" -> dailyDesignPromptRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));
            case "DAILY_SENTENCE" -> dailySentenceRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Daily sentence not found."));
            case "BOOK" -> {
                if (userBookRepository.findByUserIdAndBookId(user.getId(), id).isEmpty()) {
                    throw new AccessDeniedException("Add this book to your library before linking it.");
                }
            }
            case "PROJECT", "GAME_PROJECT" -> gameProjectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Project not found."));
            case "PROJECT_PROBLEM" -> projectProblemRepository.findByIdAndProjectOwnerId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Project problem not found."));
            case "PROJECT_APPLICATION" -> projectApplicationRepository.findByIdAndProjectOwnerId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Project application not found."));
            case "DESIGN_DECISION" -> designDecisionRepository.findByIdAndProjectOwnerId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Design decision not found."));
            case "PLAYTEST_FINDING" -> playtestFindingRepository.findByIdAndProjectOwnerId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Playtest finding not found."));
            case "PROJECT_LENS_REVIEW" -> projectLensReviewRepository.findByIdAndProjectOwnerId(id, user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Project lens review not found."));
            default -> throw new IllegalArgumentException("Unsupported entity link type: " + type);
        }
    }

    private EntityLinkResponse toResponse(EntityLink link) {
        return new EntityLinkResponse(
                link.getId(),
                link.getSourceType(),
                link.getSourceId(),
                link.getTargetType(),
                link.getTargetId(),
                link.getRelationType(),
                link.getSourceReferenceId(),
                link.getNote(),
                link.getCreatedBy(),
                CREATED_BY_SYSTEM.equalsIgnoreCase(link.getCreatedBy()),
                link.getCreatedAt());
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void assertUserCreated(EntityLink link) {
        if (!CREATED_BY_USER.equalsIgnoreCase(link.getCreatedBy())) {
            throw new AccessDeniedException("System-created links cannot be modified from the relationship editor.");
        }
    }
}
