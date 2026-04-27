package com.bookos.backend.knowledge.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.knowledge.dto.KnowledgeObjectRequest;
import com.bookos.backend.knowledge.dto.KnowledgeObjectResponse;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class KnowledgeObjectService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ConceptRepository conceptRepository;
    private final BookNoteRepository bookNoteRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<KnowledgeObjectResponse> listKnowledgeObjects(
            String email,
            KnowledgeObjectType type,
            Long bookId,
            Long conceptId,
            String query) {
        User user = userService.getByEmailRequired(email);
        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        return knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                .stream()
                .filter(item -> type == null || item.getType() == type)
                .filter(item -> bookId == null || (item.getBook() != null && Objects.equals(item.getBook().getId(), bookId)))
                .filter(item -> conceptId == null || (item.getConcept() != null && Objects.equals(item.getConcept().getId(), conceptId)))
                .filter(item -> q == null
                        || item.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || item.getSlug().contains(q))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeObjectResponse getKnowledgeObject(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(getOwnedKnowledgeObject(user, id));
    }

    @Transactional
    public KnowledgeObjectResponse createKnowledgeObject(String email, KnowledgeObjectRequest request) {
        User user = userService.getByEmailRequired(email);
        String title = request.title().trim();
        String slug = SlugUtils.slugify(title);
        knowledgeObjectRepository.findByUserIdAndTypeAndSlugAndArchivedFalse(user.getId(), request.type(), slug)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A knowledge object with this type and title already exists.");
                });

        KnowledgeObject knowledgeObject = new KnowledgeObject();
        knowledgeObject.setUser(user);
        knowledgeObject.setType(request.type());
        applyRequest(user, knowledgeObject, request);
        return toResponse(knowledgeObjectRepository.save(knowledgeObject));
    }

    @Transactional
    public KnowledgeObjectResponse updateKnowledgeObject(String email, Long id, KnowledgeObjectRequest request) {
        User user = userService.getByEmailRequired(email);
        KnowledgeObject knowledgeObject = getOwnedKnowledgeObject(user, id);
        String slug = SlugUtils.slugify(request.title().trim());
        knowledgeObjectRepository.findByUserIdAndTypeAndSlugAndArchivedFalse(user.getId(), request.type(), slug)
                .filter(existing -> !Objects.equals(existing.getId(), knowledgeObject.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A knowledge object with this type and title already exists.");
                });

        knowledgeObject.setType(request.type());
        applyRequest(user, knowledgeObject, request);
        return toResponse(knowledgeObjectRepository.save(knowledgeObject));
    }

    @Transactional
    public void archiveKnowledgeObject(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        KnowledgeObject knowledgeObject = getOwnedKnowledgeObject(user, id);
        knowledgeObject.setArchived(true);
        knowledgeObjectRepository.save(knowledgeObject);
    }

    @Transactional(readOnly = true)
    public KnowledgeObject getOwnedKnowledgeObject(User user, Long id) {
        return knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
    }

    private void applyRequest(User user, KnowledgeObject knowledgeObject, KnowledgeObjectRequest request) {
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        Book book = request.bookId() == null ? null : getLibraryBook(user, request.bookId());
        BookNote note = request.noteId() == null ? null : getOwnedNote(user, request.noteId());
        Concept concept = request.conceptId() == null ? null : getOwnedConcept(user, request.conceptId());

        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }
        if (note != null) {
            book = resolveBookFromNote(book, note);
        }

        knowledgeObject.setTitle(request.title().trim());
        knowledgeObject.setSlug(SlugUtils.slugify(request.title().trim()));
        knowledgeObject.setDescription(trimToNull(request.description()));
        knowledgeObject.setVisibility(request.visibility() == null ? Visibility.PRIVATE : request.visibility());
        knowledgeObject.setBook(book);
        knowledgeObject.setNote(note);
        knowledgeObject.setConcept(concept);
        knowledgeObject.setSourceReferenceId(sourceReference == null ? null : sourceReference.getId());
        knowledgeObject.setTagsJson(writeList(cleanTags(request.tags())));
    }

    private KnowledgeObjectResponse toResponse(KnowledgeObject knowledgeObject) {
        SourceReferenceResponse sourceReference = knowledgeObject.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(
                                knowledgeObject.getSourceReferenceId(), knowledgeObject.getUser().getId())
                        .map(this::toSourceResponse)
                        .orElse(null);

        return new KnowledgeObjectResponse(
                knowledgeObject.getId(),
                knowledgeObject.getType(),
                knowledgeObject.getTitle(),
                knowledgeObject.getSlug(),
                knowledgeObject.getDescription(),
                knowledgeObject.getVisibility(),
                knowledgeObject.getBook() == null ? null : knowledgeObject.getBook().getId(),
                knowledgeObject.getBook() == null ? null : knowledgeObject.getBook().getTitle(),
                knowledgeObject.getNote() == null ? null : knowledgeObject.getNote().getId(),
                knowledgeObject.getNote() == null ? null : knowledgeObject.getNote().getTitle(),
                knowledgeObject.getConcept() == null ? null : knowledgeObject.getConcept().getId(),
                knowledgeObject.getConcept() == null ? null : knowledgeObject.getConcept().getName(),
                sourceReference,
                readList(knowledgeObject.getTagsJson()),
                knowledgeObject.getCreatedAt(),
                knowledgeObject.getUpdatedAt());
    }

    private SourceReferenceResponse toSourceResponse(SourceReference sourceReference) {
        return new SourceReferenceResponse(
                sourceReference.getId(),
                sourceReference.getSourceType(),
                sourceReference.getBook().getId(),
                sourceReference.getNote() != null ? sourceReference.getNote().getId() : null,
                sourceReference.getNoteBlock() != null ? sourceReference.getNoteBlock().getId() : null,
                sourceReference.getRawCaptureId(),
                sourceReference.getPageStart(),
                sourceReference.getPageEnd(),
                sourceReference.getLocationLabel(),
                sourceReference.getSourceText(),
                sourceReference.getSourceConfidence());
    }

    private Book getLibraryBook(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before creating knowledge objects.");
        }
        return bookService.getBookEntity(bookId);
    }

    private BookNote getOwnedNote(User user, Long noteId) {
        return bookNoteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Note not found."));
    }

    private Concept getOwnedConcept(User user, Long conceptId) {
        return conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Concept not found."));
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

    private Book resolveBookFromNote(Book requestedBook, BookNote note) {
        Book noteBook = note.getBook();
        if (requestedBook != null && !Objects.equals(requestedBook.getId(), noteBook.getId())) {
            throw new IllegalArgumentException("Note belongs to a different book.");
        }
        return noteBook;
    }

    private List<String> cleanTags(List<String> tags) {
        Set<String> values = new LinkedHashSet<>();
        if (tags == null) {
            return List.of();
        }
        for (String tag : tags) {
            if (StringUtils.hasText(tag)) {
                values.add(tag.trim().toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(values);
    }

    private String writeList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize knowledge object tags.", exception);
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
