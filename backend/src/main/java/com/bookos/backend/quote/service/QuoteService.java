package com.bookos.backend.quote.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.quote.dto.QuoteRequest;
import com.bookos.backend.quote.dto.QuoteResponse;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
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
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final SourceReferenceService sourceReferenceService;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public List<QuoteResponse> listQuotes(String email, Long bookId, String query) {
        User user = userService.getByEmailRequired(email);
        List<Quote> quotes = bookId == null
                ? quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                : quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        return quotes.stream()
                .filter(quote -> q == null
                        || quote.getText().toLowerCase(Locale.ROOT).contains(q)
                        || (quote.getAttribution() != null && quote.getAttribution().toLowerCase(Locale.ROOT).contains(q)))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuote(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getOwnedQuote(user, id));
    }

    @Transactional
    public QuoteResponse createQuote(String email, QuoteRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = getLibraryBook(user, request.bookId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }

        Quote quote = new Quote();
        quote.setUser(user);
        quote.setBook(book);
        quote.setText(request.text().trim());
        quote.setAttribution(trimToNull(request.attribution()));
        quote.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());
        applySource(quote, sourceReference);
        return toResponse(quoteRepository.save(quote));
    }

    @Transactional
    public QuoteResponse updateQuote(String email, Long id, QuoteRequest request) {
        User user = userService.getByEmailRequired(email);
        Quote quote = getOwnedQuote(user, id);
        Book book = getLibraryBook(user, request.bookId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }

        quote.setBook(book);
        quote.setText(request.text().trim());
        quote.setAttribution(trimToNull(request.attribution()));
        quote.setVisibility(request.visibility() == null ? quote.getVisibility() : request.visibility());
        applySource(quote, sourceReference);
        return toResponse(quoteRepository.save(quote));
    }

    @Transactional
    public void archiveQuote(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        Quote quote = getOwnedQuote(user, id);
        quote.setArchived(true);
        quoteRepository.save(quote);
    }

    @Transactional
    public QuoteResponse createFromCapture(User user, RawCapture capture) {
        SourceReference sourceReference = firstCaptureSource(capture);
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setBook(capture.getBook());
        quote.setRawCaptureId(capture.getId());
        quote.setText(resolveText(capture.getCleanText(), capture.getRawText()));
        quote.setVisibility(Visibility.PRIVATE);
        applySource(quote, sourceReference);
        return toResponse(quoteRepository.save(quote));
    }

    @Transactional(readOnly = true)
    public Quote getOwnedQuote(User user, Long id) {
        return quoteRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Quote not found."));
    }

    private void applySource(Quote quote, SourceReference sourceReference) {
        quote.setSourceReferenceId(sourceReference == null ? null : sourceReference.getId());
        quote.setNote(sourceReference == null ? null : sourceReference.getNote());
        quote.setNoteBlock(sourceReference == null ? null : sourceReference.getNoteBlock());
        quote.setRawCaptureId(sourceReference == null ? quote.getRawCaptureId() : sourceReference.getRawCaptureId());
        quote.setPageStart(sourceReference == null ? null : sourceReference.getPageStart());
        quote.setPageEnd(sourceReference == null ? null : sourceReference.getPageEnd());
    }

    private QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getBook().getId(),
                quote.getBook().getTitle(),
                quote.getNote() == null ? null : quote.getNote().getId(),
                quote.getNoteBlock() == null ? null : quote.getNoteBlock().getId(),
                quote.getRawCaptureId(),
                quote.getText(),
                quote.getAttribution(),
                quote.getPageStart(),
                quote.getPageEnd(),
                quote.getVisibility(),
                quote.getSourceReferenceId() == null
                        ? null
                        : sourceReferenceRepository.findByIdAndUserId(quote.getSourceReferenceId(), quote.getUser().getId())
                                .map(sourceReferenceService::toResponse)
                                .orElse(null),
                quote.getCreatedAt(),
                quote.getUpdatedAt());
    }

    private SourceReference firstCaptureSource(RawCapture capture) {
        return sourceReferenceRepository.findByRawCaptureIdOrderByCreatedAtAsc(capture.getId())
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Book getLibraryBook(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before creating quotes.");
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

    private String resolveText(String cleanText, String rawText) {
        return StringUtils.hasText(cleanText) ? cleanText.trim() : rawText.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
