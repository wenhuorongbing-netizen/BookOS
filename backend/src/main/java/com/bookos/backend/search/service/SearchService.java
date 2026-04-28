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
