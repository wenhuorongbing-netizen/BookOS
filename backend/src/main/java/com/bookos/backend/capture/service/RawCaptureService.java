package com.bookos.backend.capture.service;

import com.bookos.backend.action.service.ActionItemService;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.dto.CaptureConversionResponse;
import com.bookos.backend.capture.dto.CaptureConversionTarget;
import com.bookos.backend.capture.dto.RawCaptureConvertRequest;
import com.bookos.backend.capture.dto.RawCaptureRequest;
import com.bookos.backend.capture.dto.RawCaptureResponse;
import com.bookos.backend.capture.dto.RawCaptureUpdateRequest;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.knowledge.service.ConceptService;
import com.bookos.backend.note.dto.BookNoteRequest;
import com.bookos.backend.note.service.BookNoteService;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import com.bookos.backend.parser.service.NoteParserService;
import com.bookos.backend.quote.service.QuoteService;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RawCaptureService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final RawCaptureRepository rawCaptureRepository;
    private final UserBookRepository userBookRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserService userService;
    private final BookService bookService;
    private final BookNoteService bookNoteService;
    private final QuoteService quoteService;
    private final ActionItemService actionItemService;
    private final NoteParserService noteParserService;
    private final SourceReferenceService sourceReferenceService;
    private final ConceptService conceptService;
    private final ObjectMapper objectMapper;

    @Transactional
    public RawCaptureResponse createCapture(String email, RawCaptureRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getBookEntity(request.bookId());
        assertBookInLibrary(user, book.getId());

        ParsedNoteResponse parsed = noteParserService.parse(request.rawText());
        RawCapture capture = new RawCapture();
        capture.setUser(user);
        capture.setBook(book);
        capture.setStatus(CaptureStatus.INBOX);
        applyParsedCapture(capture, parsed);

        RawCapture saved = rawCaptureRepository.save(capture);
        SourceReference sourceReference = sourceReferenceService.createForRawCapture(user, book, saved.getId(), parsed);
        conceptService.indexParsedConcepts(user, book, sourceReference, parsed.concepts());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RawCaptureResponse> listInbox(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        if (bookId != null) {
            assertBookInLibrary(user, bookId);
            return rawCaptureRepository.findByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(user.getId(), bookId, CaptureStatus.INBOX)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return rawCaptureRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), CaptureStatus.INBOX)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RawCaptureResponse getCapture(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getOwnedCapture(id, user));
    }

    @Transactional
    public RawCaptureResponse updateCapture(String email, Long id, RawCaptureUpdateRequest request) {
        User user = userService.getByEmailRequired(email);
        RawCapture capture = getOwnedCapture(id, user);
        if (capture.getStatus() != CaptureStatus.INBOX) {
            throw new IllegalArgumentException("Only inbox captures can be edited.");
        }

        ParsedNoteResponse parsed = noteParserService.parse(request.rawText());
        applyParsedCapture(capture, parsed);
        RawCapture saved = rawCaptureRepository.save(capture);
        SourceReference sourceReference = sourceReferenceService.replaceForRawCapture(user, saved.getBook(), saved.getId(), parsed);
        conceptService.indexParsedConcepts(user, saved.getBook(), sourceReference, parsed.concepts());
        return toResponse(saved);
    }

    @Transactional
    public CaptureConversionResponse convertCapture(String email, Long id, RawCaptureConvertRequest request) {
        User user = userService.getByEmailRequired(email);
        RawCapture capture = getOwnedCapture(id, user);
        if (capture.getStatus() != CaptureStatus.INBOX) {
            throw new IllegalArgumentException("Only inbox captures can be converted.");
        }

        CaptureConversionTarget target = request.targetType() == null ? CaptureConversionTarget.NOTE : request.targetType();
        String convertedEntityType;
        Long convertedEntityId;

        if (target == CaptureConversionTarget.NOTE) {
            var note = bookNoteService.createNote(
                    email,
                    capture.getBook().getId(),
                    new BookNoteRequest(resolveTitle(request.title(), capture), capture.getRawText(), Visibility.PRIVATE));
            convertedEntityType = "NOTE";
            convertedEntityId = note.id();
        } else if (target == CaptureConversionTarget.QUOTE) {
            var quote = quoteService.createFromCapture(user, capture);
            convertedEntityType = "QUOTE";
            convertedEntityId = quote.id();
        } else if (target == CaptureConversionTarget.ACTION_ITEM) {
            var actionItem = actionItemService.createFromCapture(user, capture, request.title());
            convertedEntityType = "ACTION_ITEM";
            convertedEntityId = actionItem.id();
        } else {
            throw new IllegalArgumentException("CONCEPT conversion is not available until the concept review workflow is built.");
        }

        capture.setStatus(CaptureStatus.CONVERTED);
        capture.setConvertedEntityType(convertedEntityType);
        capture.setConvertedEntityId(convertedEntityId);
        RawCapture saved = rawCaptureRepository.save(capture);

        return new CaptureConversionResponse(toResponse(saved), convertedEntityType, convertedEntityId);
    }

    @Transactional
    public RawCaptureResponse archiveCapture(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        RawCapture capture = getOwnedCapture(id, user);
        if (capture.getStatus() == CaptureStatus.CONVERTED) {
            throw new IllegalArgumentException("Converted captures cannot be archived.");
        }
        capture.setStatus(CaptureStatus.ARCHIVED);
        return toResponse(rawCaptureRepository.save(capture));
    }

    private RawCapture getOwnedCapture(Long id, User user) {
        return rawCaptureRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Capture not found."));
    }

    private void applyParsedCapture(RawCapture capture, ParsedNoteResponse parsed) {
        capture.setRawText(parsed.rawText());
        capture.setCleanText(parsed.cleanText());
        capture.setParsedType(parsed.type());
        capture.setPageStart(parsed.pageStart());
        capture.setPageEnd(parsed.pageEnd());
        capture.setTagsJson(writeList(parsed.tags()));
        capture.setConceptsJson(writeList(parsed.concepts()));
        capture.setParserWarningsJson(writeList(parsed.warnings()));
    }

    private RawCaptureResponse toResponse(RawCapture capture) {
        var sourceReferences = sourceReferenceRepository.findByRawCaptureIdOrderByCreatedAtAsc(capture.getId())
                .stream()
                .map(sourceReferenceService::toResponse)
                .toList();

        return new RawCaptureResponse(
                capture.getId(),
                capture.getBook().getId(),
                capture.getBook().getTitle(),
                capture.getRawText(),
                capture.getCleanText(),
                capture.getParsedType(),
                capture.getPageStart(),
                capture.getPageEnd(),
                readList(capture.getTagsJson()),
                readList(capture.getConceptsJson()),
                readList(capture.getParserWarningsJson()),
                capture.getStatus(),
                capture.getConvertedEntityType(),
                capture.getConvertedEntityId(),
                sourceReferences,
                capture.getCreatedAt(),
                capture.getUpdatedAt());
    }

    private void assertBookInLibrary(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before creating captures.");
        }
    }

    private String resolveTitle(String requestedTitle, RawCapture capture) {
        if (StringUtils.hasText(requestedTitle)) {
            return requestedTitle.trim();
        }

        String text = StringUtils.hasText(capture.getCleanText()) ? capture.getCleanText() : capture.getRawText();
        String firstLine = text.lines().filter(StringUtils::hasText).findFirst().orElse("Converted capture");
        return firstLine.length() > 80 ? firstLine.substring(0, 77) + "..." : firstLine;
    }

    private String writeList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize parser metadata.", exception);
        }
    }

    private List<String> readList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, STRING_LIST);
        } catch (Exception exception) {
            return List.of();
        }
    }
}
