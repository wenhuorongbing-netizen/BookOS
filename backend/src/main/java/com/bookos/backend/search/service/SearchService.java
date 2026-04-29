package com.bookos.backend.search.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.dto.BookResponse;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.project.entity.DesignDecision;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.PlaytestFinding;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.entity.ProjectLensReview;
import com.bookos.backend.project.entity.ProjectProblem;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.search.dto.SearchResultResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_RESULTS = 50;

    private final BookService bookService;
    private final BookNoteRepository bookNoteRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserService userService;
    private final UserBookRepository userBookRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectProblemRepository projectProblemRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DesignDecisionRepository designDecisionRepository;
    private final PlaytestFindingRepository playtestFindingRepository;
    private final ProjectLensReviewRepository projectLensReviewRepository;

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String email, String query, String type, Long bookId) {
        User user = userService.getByEmailRequired(email);
        String q = normalizeQuery(query);
        String wantedType = normalizeType(type);
        List<SearchResultResponse> results = new ArrayList<>();

        if (matchesType(wantedType, "BOOK")) {
            bookService.listBooks(email, q, null, null).stream()
                    .filter(book -> bookId == null || Objects.equals(book.id(), bookId))
                    .filter(book -> matches(q, book.title(), book.subtitle(), book.description(), book.category(), String.join(" ", book.authors())))
                    .map(book -> new SearchResultResponse(
                            "BOOK",
                            book.id(),
                            book.title(),
                            excerpt(firstText(book.description(), book.subtitle(), String.join(", ", book.authors()))),
                            book.id(),
                            book.title(),
                            null,
                            null))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "NOTE")) {
            bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(note -> bookId == null || Objects.equals(note.getBook().getId(), bookId))
                    .filter(note -> matches(q, note.getTitle(), note.getMarkdown(), note.getThreeSentenceSummary()))
                    .map(note -> new SearchResultResponse(
                            "NOTE",
                            note.getId(),
                            note.getTitle(),
                            excerpt(firstText(note.getThreeSentenceSummary(), note.getMarkdown())),
                            note.getBook().getId(),
                            note.getBook().getTitle(),
                            firstSourceReferenceIdForNote(user, note.getId()),
                            note.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "CAPTURE")) {
            List<RawCapture> captures = new ArrayList<>();
            captures.addAll(bookId == null
                    ? rawCaptureRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CaptureStatus.INBOX)
                    : rawCaptureRepository.findByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(user.getId(), bookId, CaptureStatus.INBOX));
            captures.addAll(bookId == null
                    ? rawCaptureRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CaptureStatus.CONVERTED)
                    : rawCaptureRepository.findByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(user.getId(), bookId, CaptureStatus.CONVERTED));
            captures.stream()
                    .filter(capture -> bookId == null || Objects.equals(capture.getBook().getId(), bookId))
                    .filter(capture -> matches(q, capture.getRawText(), capture.getCleanText()))
                    .map(capture -> new SearchResultResponse(
                            "CAPTURE",
                            capture.getId(),
                            titleFromText(capture.getCleanText(), capture.getRawText(), "Raw capture"),
                            excerpt(firstText(capture.getCleanText(), capture.getRawText())),
                            capture.getBook().getId(),
                            capture.getBook().getTitle(),
                            firstSourceReferenceIdForCapture(user, capture.getId()),
                            capture.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "QUOTE")) {
            (bookId == null
                            ? quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                            : quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId))
                    .stream()
                    .filter(quote -> matches(q, quote.getText(), quote.getAttribution()))
                    .map(quote -> new SearchResultResponse(
                            "QUOTE",
                            quote.getId(),
                            titleFromText(quote.getText(), null, "Quote"),
                            excerpt(quote.getText()),
                            quote.getBook().getId(),
                            quote.getBook().getTitle(),
                            quote.getSourceReferenceId(),
                            quote.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "ACTION_ITEM")) {
            (bookId == null
                            ? actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                            : actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId))
                    .stream()
                    .filter(item -> matches(q, item.getTitle(), item.getDescription()))
                    .map(item -> new SearchResultResponse(
                            "ACTION_ITEM",
                            item.getId(),
                            item.getTitle(),
                            excerpt(item.getDescription()),
                            item.getBook().getId(),
                            item.getBook().getTitle(),
                            item.getSourceReferenceId(),
                            item.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "CONCEPT")) {
            conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(concept -> bookId == null || concept.getFirstBook() != null && Objects.equals(concept.getFirstBook().getId(), bookId))
                    .filter(concept -> matches(q, concept.getName(), concept.getDescription()))
                    .map(concept -> new SearchResultResponse(
                            "CONCEPT",
                            concept.getId(),
                            concept.getName(),
                            excerpt(concept.getDescription()),
                            concept.getFirstBook() == null ? null : concept.getFirstBook().getId(),
                            concept.getFirstBook() == null ? null : concept.getFirstBook().getTitle(),
                            concept.getFirstSourceReference() == null ? null : concept.getFirstSourceReference().getId(),
                            concept.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "KNOWLEDGE_OBJECT")) {
            knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(object -> bookId == null || object.getBook() != null && Objects.equals(object.getBook().getId(), bookId))
                    .filter(object -> matches(q, object.getTitle(), object.getDescription(), object.getType().name()))
                    .map(object -> new SearchResultResponse(
                            "KNOWLEDGE_OBJECT",
                            object.getId(),
                            object.getTitle(),
                            excerpt(object.getDescription()),
                            object.getBook() == null ? null : object.getBook().getId(),
                            object.getBook() == null ? null : object.getBook().getTitle(),
                            object.getSourceReferenceId(),
                            object.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "FORUM_THREAD")) {
            forumThreadRepository.findByStatusNotOrderByUpdatedAtDesc(ForumThreadStatus.ARCHIVED).stream()
                    .filter(thread -> canSeeThread(user, thread))
                    .filter(thread -> bookId == null
                            || visibleForumBook(user, thread) != null
                                    && Objects.equals(visibleForumBook(user, thread).getId(), bookId))
                    .filter(thread -> matches(q, thread.getTitle(), thread.getBodyMarkdown(), thread.getCategory().getName()))
                    .map(thread -> {
                        Book visibleBook = visibleForumBook(user, thread);
                        return new SearchResultResponse(
                                "FORUM_THREAD",
                                thread.getId(),
                                thread.getTitle(),
                                excerpt(thread.getBodyMarkdown()),
                                visibleBook == null ? null : visibleBook.getId(),
                                visibleBook == null ? null : visibleBook.getTitle(),
                                visibleSourceReferenceId(user, thread.getSourceReferenceId()),
                                thread.getUpdatedAt());
                    })
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "GAME_PROJECT")) {
            projectRepository.findByOwnerIdAndArchivedAtIsNullOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(project -> matches(q, project.getTitle(), project.getDescription(), project.getGenre(), project.getPlatform(), project.getStage()))
                    .map(project -> new SearchResultResponse(
                            "GAME_PROJECT",
                            project.getId(),
                            project.getTitle(),
                            excerpt(firstText(project.getDescription(), project.getGenre(), project.getPlatform(), project.getStage())),
                            null,
                            null,
                            project.getId(),
                            project.getTitle(),
                            null,
                            project.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "PROJECT_PROBLEM")) {
            projectProblemRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(problem -> matches(q, problem.getTitle(), problem.getDescription(), problem.getStatus(), problem.getPriority(), problem.getProject().getTitle()))
                    .map(problem -> new SearchResultResponse(
                            "PROJECT_PROBLEM",
                            problem.getId(),
                            problem.getTitle(),
                            excerpt(problem.getDescription()),
                            sourceBookId(problem.getRelatedSourceReference()),
                            sourceBookTitle(problem.getRelatedSourceReference()),
                            problem.getProject().getId(),
                            problem.getProject().getTitle(),
                            sourceReferenceId(problem.getRelatedSourceReference()),
                            problem.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "PROJECT_APPLICATION")) {
            projectApplicationRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(application -> matches(q, application.getTitle(), application.getDescription(), application.getApplicationType(), application.getStatus(), application.getProject().getTitle()))
                    .map(application -> new SearchResultResponse(
                            "PROJECT_APPLICATION",
                            application.getId(),
                            application.getTitle(),
                            excerpt(application.getDescription()),
                            sourceBookId(application.getSourceReference()),
                            sourceBookTitle(application.getSourceReference()),
                            application.getProject().getId(),
                            application.getProject().getTitle(),
                            sourceReferenceId(application.getSourceReference()),
                            application.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "DESIGN_DECISION")) {
            designDecisionRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(decision -> matches(q, decision.getTitle(), decision.getDecision(), decision.getRationale(), decision.getTradeoffs(), decision.getStatus(), decision.getProject().getTitle()))
                    .map(decision -> new SearchResultResponse(
                            "DESIGN_DECISION",
                            decision.getId(),
                            decision.getTitle(),
                            excerpt(firstText(decision.getDecision(), decision.getRationale(), decision.getTradeoffs())),
                            sourceBookId(decision.getSourceReference()),
                            sourceBookTitle(decision.getSourceReference()),
                            decision.getProject().getId(),
                            decision.getProject().getTitle(),
                            sourceReferenceId(decision.getSourceReference()),
                            decision.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "PLAYTEST_FINDING")) {
            playtestFindingRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(finding -> matches(q, finding.getTitle(), finding.getObservation(), finding.getRecommendation(), finding.getSeverity(), finding.getStatus(), finding.getProject().getTitle()))
                    .map(finding -> new SearchResultResponse(
                            "PLAYTEST_FINDING",
                            finding.getId(),
                            finding.getTitle(),
                            excerpt(firstText(finding.getObservation(), finding.getRecommendation())),
                            sourceBookId(finding.getSourceReference()),
                            sourceBookTitle(finding.getSourceReference()),
                            finding.getProject().getId(),
                            finding.getProject().getTitle(),
                            sourceReferenceId(finding.getSourceReference()),
                            finding.getUpdatedAt()))
                    .forEach(results::add);
        }

        if (matchesType(wantedType, "PROJECT_LENS_REVIEW")) {
            projectLensReviewRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .filter(review -> matches(q, review.getQuestion(), review.getAnswer(), review.getStatus(), review.getProject().getTitle(),
                            review.getKnowledgeObject() == null ? null : review.getKnowledgeObject().getTitle()))
                    .map(review -> new SearchResultResponse(
                            "PROJECT_LENS_REVIEW",
                            review.getId(),
                            review.getQuestion(),
                            excerpt(review.getAnswer()),
                            sourceBookId(review.getSourceReference()),
                            sourceBookTitle(review.getSourceReference()),
                            review.getProject().getId(),
                            review.getProject().getTitle(),
                            sourceReferenceId(review.getSourceReference()),
                            review.getUpdatedAt()))
                    .forEach(results::add);
        }

        return results.stream()
                .sorted(Comparator.comparing(SearchResultResponse::updatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_RESULTS)
                .toList();
    }

    private boolean canSeeThread(User user, ForumThread thread) {
        return thread.getVisibility() == Visibility.PUBLIC
                || thread.getVisibility() == Visibility.SHARED
                || Objects.equals(thread.getAuthor().getId(), user.getId());
    }

    private Book visibleForumBook(User user, ForumThread thread) {
        Book book = thread.getRelatedBook();
        if (book == null) {
            return null;
        }
        return canReadBook(user, book) ? book : null;
    }

    private boolean canReadBook(User viewer, Book book) {
        return book.getVisibility() == Visibility.PUBLIC
                || book.getVisibility() == Visibility.SHARED
                || book.getOwner() != null && Objects.equals(book.getOwner().getId(), viewer.getId())
                || userBookRepository.findByUserIdAndBookId(viewer.getId(), book.getId()).isPresent();
    }

    private Long visibleSourceReferenceId(User user, Long sourceReferenceId) {
        if (sourceReferenceId == null) {
            return null;
        }
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId())
                .map(SourceReference::getId)
                .orElse(null);
    }

    private Long firstSourceReferenceIdForNote(User user, Long noteId) {
        return sourceReferenceRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(noteId, user.getId()).stream()
                .findFirst()
                .map(SourceReference::getId)
                .orElse(null);
    }

    private Long firstSourceReferenceIdForCapture(User user, Long captureId) {
        return sourceReferenceRepository.findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(captureId, user.getId()).stream()
                .findFirst()
                .map(SourceReference::getId)
                .orElse(null);
    }

    private Long sourceReferenceId(SourceReference sourceReference) {
        return sourceReference == null ? null : sourceReference.getId();
    }

    private Long sourceBookId(SourceReference sourceReference) {
        return sourceReference == null || sourceReference.getBook() == null ? null : sourceReference.getBook().getId();
    }

    private String sourceBookTitle(SourceReference sourceReference) {
        return sourceReference == null || sourceReference.getBook() == null ? null : sourceReference.getBook().getTitle();
    }

    private boolean matchesType(String wantedType, String type) {
        return wantedType == null || wantedType.equals(type);
    }

    private boolean matches(String query, String... values) {
        if (!StringUtils.hasText(query)) {
            return true;
        }
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeQuery(String query) {
        return StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String normalizeType(String type) {
        return StringUtils.hasText(type) ? type.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String titleFromText(String first, String second, String fallback) {
        String value = firstText(first, second);
        if (value == null) {
            return fallback;
        }
        String clean = value.replaceAll("\\s+", " ").trim();
        return clean.length() > 72 ? clean.substring(0, 69) + "..." : clean;
    }

    private String excerpt(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String clean = value.replaceAll("\\s+", " ").trim();
        return clean.length() > 240 ? clean.substring(0, 237) + "..." : clean;
    }
}
