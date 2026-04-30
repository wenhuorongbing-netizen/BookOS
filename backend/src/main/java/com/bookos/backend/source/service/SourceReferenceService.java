package com.bookos.backend.source.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.daily.repository.DailySentenceRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.knowledge.service.ConceptService;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectKnowledgeLinkRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SourceReferenceService {

    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserService userService;
    private final ConceptService conceptService;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final DailySentenceRepository dailySentenceRepository;
    private final ProjectProblemRepository projectProblemRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DesignDecisionRepository designDecisionRepository;
    private final PlaytestFindingRepository playtestFindingRepository;
    private final ProjectLensReviewRepository projectLensReviewRepository;
    private final ProjectKnowledgeLinkRepository projectKnowledgeLinkRepository;

    @Transactional(readOnly = true)
    public SourceReferenceResponse getSourceReference(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getOwnedSourceReference(user, id));
    }

    @Transactional(readOnly = true)
    public List<SourceReferenceResponse> listSourceReferences(String email, Long bookId, String entityType, Long entityId) {
        User user = userService.getByEmailRequired(email);
        if (StringUtils.hasText(entityType) && entityId != null) {
            return listByEntity(user, normalize(entityType), entityId).stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (bookId != null) {
            return sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, user.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return sourceReferenceRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SourceReferenceResponse> listBookSources(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        return sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SourceReferenceResponse> listNoteSources(String email, Long noteId) {
        User user = userService.getByEmailRequired(email);
        return sourceReferenceRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(noteId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SourceReferenceResponse> listCaptureSources(String email, Long captureId) {
        User user = userService.getByEmailRequired(email);
        return sourceReferenceRepository.findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(captureId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SourceReference getOwnedSourceReference(User user, Long id) {
        return sourceReferenceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
    }

    @Transactional
    public SourceReference createForNoteBlock(User user, Book book, BookNote note, NoteBlock block, ParsedNoteResponse parsed) {
        SourceReference sourceReference = new SourceReference();
        sourceReference.setUser(user);
        sourceReference.setBook(book);
        sourceReference.setNote(note);
        sourceReference.setNoteBlock(block);
        sourceReference.setSourceType("NOTE_BLOCK");
        sourceReference.setPageStart(parsed.pageStart());
        sourceReference.setPageEnd(parsed.pageEnd());
        sourceReference.setLocationLabel(buildLocationLabel(parsed));
        sourceReference.setSourceText(parsed.rawText());
        sourceReference.setSourceConfidence(parsed.pageStart() == null ? SourceConfidence.LOW : SourceConfidence.HIGH);
        return sourceReferenceRepository.save(sourceReference);
    }

    @Transactional
    public SourceReference createForRawCapture(User user, Book book, Long rawCaptureId, ParsedNoteResponse parsed) {
        SourceReference sourceReference = new SourceReference();
        sourceReference.setUser(user);
        sourceReference.setBook(book);
        sourceReference.setRawCaptureId(rawCaptureId);
        sourceReference.setSourceType("RAW_CAPTURE");
        sourceReference.setPageStart(parsed.pageStart());
        sourceReference.setPageEnd(parsed.pageEnd());
        sourceReference.setLocationLabel(buildLocationLabel(parsed, "Raw capture"));
        sourceReference.setSourceText(parsed.rawText());
        sourceReference.setSourceConfidence(parsed.pageStart() == null ? SourceConfidence.LOW : SourceConfidence.HIGH);
        return sourceReferenceRepository.save(sourceReference);
    }

    @Transactional
    public SourceReference replaceForNoteBlock(User user, Book book, BookNote note, NoteBlock block, ParsedNoteResponse parsed) {
        sourceReferenceRepository.findByNoteBlockIdOrderByCreatedAtAsc(block.getId())
                .forEach(source -> conceptService.removeLinksForSourceReference(user, source.getId()));
        sourceReferenceRepository.deleteByNoteBlockId(block.getId());
        return createForNoteBlock(user, book, note, block, parsed);
    }

    @Transactional
    public SourceReference replaceForRawCapture(User user, Book book, Long rawCaptureId, ParsedNoteResponse parsed) {
        sourceReferenceRepository.findByRawCaptureIdOrderByCreatedAtAsc(rawCaptureId)
                .forEach(source -> conceptService.removeLinksForSourceReference(user, source.getId()));
        sourceReferenceRepository.deleteByRawCaptureId(rawCaptureId);
        return createForRawCapture(user, book, rawCaptureId, parsed);
    }

    public SourceReferenceResponse toResponse(SourceReference sourceReference) {
        return new SourceReferenceResponse(
                sourceReference.getId(),
                sourceReference.getSourceType(),
                sourceReference.getBook().getId(),
                sourceReference.getNote() != null ? sourceReference.getNote().getId() : null,
                sourceReference.getNoteBlock() != null ? sourceReference.getNoteBlock().getId() : null,
                sourceReference.getChapterId(),
                sourceReference.getRawCaptureId(),
                sourceReference.getPageStart(),
                sourceReference.getPageEnd(),
                sourceReference.getLocationLabel(),
                sourceReference.getSourceText(),
                sourceReference.getSourceConfidence(),
                sourceReference.getCreatedAt());
    }

    private List<SourceReference> listByEntity(User user, String entityType, Long entityId) {
        return switch (entityType) {
            case "BOOK" -> sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(entityId, user.getId());
            case "NOTE" -> sourceReferenceRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(entityId, user.getId());
            case "NOTE_BLOCK" -> sourceReferenceRepository.findByNoteBlockIdAndUserIdOrderByCreatedAtDesc(entityId, user.getId());
            case "RAW_CAPTURE", "CAPTURE" -> sourceReferenceRepository.findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(entityId, user.getId());
            case "QUOTE" -> quoteRepository.findByIdAndUserIdAndArchivedFalse(entityId, user.getId())
                    .map(quote -> sourceById(user, quote.getSourceReferenceId()))
                    .orElse(List.of());
            case "ACTION_ITEM" -> actionItemRepository.findByIdAndUserIdAndArchivedFalse(entityId, user.getId())
                    .map(item -> sourceById(user, item.getSourceReferenceId()))
                    .orElse(List.of());
            case "CONCEPT" -> conceptRepository.findByIdAndUserIdAndArchivedFalse(entityId, user.getId())
                    .map(concept -> concept.getFirstSourceReference() == null
                            ? List.<SourceReference>of()
                            : sourceById(user, concept.getFirstSourceReference().getId()))
                    .orElse(List.of());
            case "KNOWLEDGE_OBJECT" -> knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(entityId, user.getId())
                    .map(object -> sourceById(user, object.getSourceReferenceId()))
                    .orElse(List.of());
            case "FORUM_THREAD" -> forumThreadRepository.findByIdAndStatusNot(entityId, ForumThreadStatus.ARCHIVED)
                    .map(thread -> sourceById(user, thread.getSourceReferenceId()))
                    .orElse(List.of());
            case "DAILY_PROMPT", "DAILY_DESIGN_PROMPT" -> dailyDesignPromptRepository.findByIdAndUserId(entityId, user.getId())
                    .map(prompt -> sourceById(user, prompt.getSourceReferenceId()))
                    .orElse(List.of());
            case "DAILY_SENTENCE" -> dailySentenceRepository.findByIdAndUserId(entityId, user.getId())
                    .map(sentence -> sourceById(user, sentence.getSourceReferenceId()))
                    .orElse(List.of());
            case "PROJECT_PROBLEM" -> projectProblemRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(problem -> sourceById(user, problem.getRelatedSourceReference() == null ? null : problem.getRelatedSourceReference().getId()))
                    .orElse(List.of());
            case "PROJECT_APPLICATION" -> projectApplicationRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(application -> sourceById(user, application.getSourceReference() == null ? null : application.getSourceReference().getId()))
                    .orElse(List.of());
            case "DESIGN_DECISION" -> designDecisionRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(decision -> sourceById(user, decision.getSourceReference() == null ? null : decision.getSourceReference().getId()))
                    .orElse(List.of());
            case "PLAYTEST_FINDING" -> playtestFindingRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(finding -> sourceById(user, finding.getSourceReference() == null ? null : finding.getSourceReference().getId()))
                    .orElse(List.of());
            case "PROJECT_LENS_REVIEW" -> projectLensReviewRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(review -> sourceById(user, review.getSourceReference() == null ? null : review.getSourceReference().getId()))
                    .orElse(List.of());
            case "PROJECT_KNOWLEDGE_LINK" -> projectKnowledgeLinkRepository.findByIdAndProjectOwnerId(entityId, user.getId())
                    .map(link -> sourceById(user, link.getSourceReference() == null ? null : link.getSourceReference().getId()))
                    .orElse(List.of());
            case "SOURCE_REFERENCE" -> sourceReferenceRepository.findByIdAndUserId(entityId, user.getId()).stream().toList();
            default -> throw new IllegalArgumentException("Unsupported source reference entity type: " + entityType);
        };
    }

    private List<SourceReference> sourceById(User user, Long sourceReferenceId) {
        if (sourceReferenceId == null) {
            return List.of();
        }
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId()).stream().toList();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String buildLocationLabel(ParsedNoteResponse parsed) {
        return buildLocationLabel(parsed, "Note block");
    }

    private String buildLocationLabel(ParsedNoteResponse parsed, String fallback) {
        if (parsed.pageStart() == null) {
            return fallback;
        }
        if (parsed.pageEnd() != null) {
            return "p." + parsed.pageStart() + "-" + parsed.pageEnd();
        }
        return "p." + parsed.pageStart();
    }
}
