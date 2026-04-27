package com.bookos.backend.source.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.knowledge.service.ConceptService;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
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
            case "SOURCE_REFERENCE" -> sourceReferenceRepository.findByIdAndUserId(entityId, user.getId()).stream().toList();
            default -> throw new IllegalArgumentException("Unsupported source reference entity type: " + entityType);
        };
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
