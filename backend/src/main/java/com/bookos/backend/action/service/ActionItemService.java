package com.bookos.backend.action.service;

import com.bookos.backend.action.dto.ActionItemRequest;
import com.bookos.backend.action.dto.ActionItemResponse;
import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final SourceReferenceService sourceReferenceService;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public List<ActionItemResponse> listActionItems(String email, Long bookId, Boolean completed, String query) {
        User user = userService.getByEmailRequired(email);
        List<ActionItem> items = bookId == null
                ? actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                : actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        return items.stream()
                .filter(item -> completed == null || (item.getCompletedAt() != null) == completed)
                .filter(item -> q == null
                        || item.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || (item.getDescription() != null && item.getDescription().toLowerCase(Locale.ROOT).contains(q)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActionItemResponse getActionItem(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getOwnedActionItem(user, id));
    }

    @Transactional
    public ActionItemResponse createActionItem(String email, ActionItemRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = getLibraryBook(user, request.bookId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }

        ActionItem item = new ActionItem();
        item.setUser(user);
        item.setBook(book);
        applyRequest(item, request);
        applySource(item, sourceReference);
        applyManualSourceFields(item, request, sourceReference);
        return toResponse(actionItemRepository.save(item));
    }

    @Transactional
    public ActionItemResponse updateActionItem(String email, Long id, ActionItemRequest request) {
        User user = userService.getByEmailRequired(email);
        ActionItem item = getOwnedActionItem(user, id);
        Book book = getLibraryBook(user, request.bookId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }

        item.setBook(book);
        applyRequest(item, request);
        applySource(item, sourceReference);
        applyManualSourceFields(item, request, sourceReference);
        return toResponse(actionItemRepository.save(item));
    }

    @Transactional
    public ActionItemResponse completeActionItem(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ActionItem item = getOwnedActionItem(user, id);
        item.setCompletedAt(Instant.now());
        return toResponse(actionItemRepository.save(item));
    }

    @Transactional
    public ActionItemResponse reopenActionItem(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ActionItem item = getOwnedActionItem(user, id);
        item.setCompletedAt(null);
        return toResponse(actionItemRepository.save(item));
    }

    @Transactional
    public void archiveActionItem(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ActionItem item = getOwnedActionItem(user, id);
        item.setArchived(true);
        actionItemRepository.save(item);
    }

    @Transactional
    public ActionItemResponse createFromCapture(User user, RawCapture capture, String requestedTitle) {
        SourceReference sourceReference = firstCaptureSource(capture);
        ActionItem item = new ActionItem();
        item.setUser(user);
        item.setBook(capture.getBook());
        item.setRawCaptureId(capture.getId());
        item.setTitle(resolveTitle(requestedTitle, capture.getCleanText(), capture.getRawText()));
        item.setDescription(resolveDescription(capture.getCleanText(), capture.getRawText()));
        item.setPriority(ActionPriority.MEDIUM);
        item.setVisibility(Visibility.PRIVATE);
        applySource(item, sourceReference);
        return toResponse(actionItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public ActionItem getOwnedActionItem(User user, Long id) {
        return actionItemRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Action item not found."));
    }

    private void applyRequest(ActionItem item, ActionItemRequest request) {
        item.setTitle(request.title().trim());
        item.setDescription(trimToNull(request.description()));
        item.setPriority(request.priority() == null ? ActionPriority.MEDIUM : request.priority());
        item.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());
    }

    private void applySource(ActionItem item, SourceReference sourceReference) {
        item.setSourceReferenceId(sourceReference == null ? null : sourceReference.getId());
        item.setNote(sourceReference == null ? null : sourceReference.getNote());
        item.setNoteBlock(sourceReference == null ? null : sourceReference.getNoteBlock());
        item.setRawCaptureId(sourceReference == null ? item.getRawCaptureId() : sourceReference.getRawCaptureId());
        item.setPageStart(sourceReference == null ? null : sourceReference.getPageStart());
        item.setPageEnd(sourceReference == null ? null : sourceReference.getPageEnd());
    }

    private void applyManualSourceFields(ActionItem item, ActionItemRequest request, SourceReference sourceReference) {
        if (sourceReference != null) {
            return;
        }

        if (request.pageStart() != null && request.pageEnd() != null && request.pageEnd() < request.pageStart()) {
            throw new IllegalArgumentException("Page end must be greater than or equal to page start.");
        }

        item.setPageStart(request.pageStart());
        item.setPageEnd(request.pageEnd());
    }

    private ActionItemResponse toResponse(ActionItem item) {
        return new ActionItemResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getNote() == null ? null : item.getNote().getId(),
                item.getNoteBlock() == null ? null : item.getNoteBlock().getId(),
                item.getRawCaptureId(),
                item.getTitle(),
                item.getDescription(),
                item.getPriority(),
                item.getPageStart(),
                item.getPageEnd(),
                item.getCompletedAt() != null,
                item.getCompletedAt(),
                item.getVisibility(),
                item.getSourceReferenceId() == null
                        ? null
                        : sourceReferenceRepository.findByIdAndUserId(item.getSourceReferenceId(), item.getUser().getId())
                                .map(sourceReferenceService::toResponse)
                                .orElse(null),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }

    private SourceReference firstCaptureSource(RawCapture capture) {
        return sourceReferenceRepository.findByRawCaptureIdOrderByCreatedAtAsc(capture.getId())
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Book getLibraryBook(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before creating action items.");
        }
        return bookService.getBookEntity(bookId);
    }

    private SourceReference getOwnedSourceReference(User user, Long sourceReferenceId) {
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
    }

    private Book resolveBookFromSource(Book requestedBook, SourceReference sourceReference) {
        Book sourceBook = sourceReference.getBook();
        if (requestedBook != null && !Objects.equals(requestedBook.getId(), sourceBook.getId())) {
            throw new IllegalArgumentException("Source reference belongs to a different book.");
        }
        return sourceBook;
    }

    private String resolveTitle(String requestedTitle, String cleanText, String rawText) {
        if (StringUtils.hasText(requestedTitle)) {
            return requestedTitle.trim();
        }
        String value = StringUtils.hasText(cleanText) ? cleanText.trim() : rawText.trim();
        return value.length() > 220 ? value.substring(0, 217) + "..." : value;
    }

    private String resolveDescription(String cleanText, String rawText) {
        return StringUtils.hasText(cleanText) ? cleanText.trim() : rawText.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
