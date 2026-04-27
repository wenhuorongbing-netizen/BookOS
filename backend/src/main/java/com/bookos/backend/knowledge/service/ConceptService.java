package com.bookos.backend.knowledge.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.knowledge.dto.ConceptRequest;
import com.bookos.backend.knowledge.dto.ConceptResponse;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ConceptService {

    public static final String ENTITY_TYPE_CONCEPT = "CONCEPT";
    public static final String ENTITY_TYPE_SOURCE_REFERENCE = "SOURCE_REFERENCE";
    public static final String RELATION_MENTIONS_CONCEPT = "MENTIONS_CONCEPT";

    private final ConceptRepository conceptRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public List<ConceptResponse> listConcepts(String email, Long bookId, String query) {
        User user = userService.getByEmailRequired(email);
        List<Concept> concepts = bookId == null
                ? conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())
                : conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);

        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        return concepts.stream()
                .filter(concept -> q == null
                        || concept.getName().toLowerCase(Locale.ROOT).contains(q)
                        || concept.getSlug().contains(q))
                .map(concept -> toResponse(user, concept))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConceptResponse getConcept(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return toResponse(user, getOwnedConcept(user, id));
    }

    @Transactional
    public ConceptResponse createConcept(String email, ConceptRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = request.bookId() == null ? null : getLibraryBook(user, request.bookId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(user, request.sourceReferenceId());
        if (sourceReference != null) {
            book = resolveBookFromSource(book, sourceReference);
        }

        Concept concept = upsertConcept(user, book, sourceReference, request.name(), request.description(), request.visibility());
        if (sourceReference != null) {
            linkSourceToConcept(user, sourceReference, concept);
        }
        return toResponse(user, concept);
    }

    @Transactional
    public ConceptResponse updateConcept(String email, Long id, ConceptRequest request) {
        User user = userService.getByEmailRequired(email);
        Concept concept = getOwnedConcept(user, id);
        String slug = SlugUtils.slugify(request.name().trim());
        conceptRepository.findByUserIdAndSlugAndArchivedFalse(user.getId(), slug)
                .filter(existing -> !Objects.equals(existing.getId(), concept.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A concept with this name already exists.");
                });

        concept.setName(request.name().trim());
        concept.setSlug(slug);
        concept.setDescription(trimToNull(request.description()));
        concept.setVisibility(request.visibility() == null ? concept.getVisibility() : request.visibility());

        if (request.bookId() != null) {
            concept.setFirstBook(getLibraryBook(user, request.bookId()));
        }
        if (request.sourceReferenceId() != null) {
            SourceReference sourceReference = getOwnedSourceReference(user, request.sourceReferenceId());
            concept.setFirstSourceReference(sourceReference);
            concept.setFirstBook(resolveBookFromSource(concept.getFirstBook(), sourceReference));
            linkSourceToConcept(user, sourceReference, concept);
        }

        return toResponse(user, conceptRepository.save(concept));
    }

    @Transactional
    public void archiveConcept(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        Concept concept = getOwnedConcept(user, id);
        concept.setArchived(true);
        conceptRepository.save(concept);
    }

    @Transactional
    public ConceptResponse reviewParsedConcept(
            User user,
            Book book,
            SourceReference sourceReference,
            String finalName,
            Long existingConceptId) {
        Concept concept;
        if (existingConceptId != null) {
            concept = getOwnedConcept(user, existingConceptId);
            if (concept.getFirstBook() == null) {
                concept.setFirstBook(book);
            }
            if (concept.getFirstSourceReference() == null) {
                concept.setFirstSourceReference(sourceReference);
            }
            concept = conceptRepository.save(concept);
        } else {
            if (!StringUtils.hasText(finalName)) {
                throw new IllegalArgumentException("Final concept name is required.");
            }
            concept = upsertConcept(user, book, sourceReference, finalName, null, Visibility.PRIVATE);
        }

        linkSourceToConcept(user, sourceReference, concept);
        return toResponse(user, concept);
    }

    @Transactional
    public void indexParsedConcepts(User user, Book book, SourceReference sourceReference, List<String> conceptNames) {
        for (String conceptName : dedupe(conceptNames)) {
            Concept concept = upsertConcept(user, book, sourceReference, conceptName, null, Visibility.PRIVATE);
            linkSourceToConcept(user, sourceReference, concept);
        }
    }

    @Transactional
    public void removeLinksForSourceReference(User user, Long sourceReferenceId) {
        List<EntityLink> links = entityLinkRepository.findByUserIdAndSourceTypeAndSourceId(
                user.getId(), ENTITY_TYPE_SOURCE_REFERENCE, sourceReferenceId);
        entityLinkRepository.deleteAll(links);
        links.stream()
                .filter(link -> ENTITY_TYPE_CONCEPT.equals(link.getTargetType()))
                .map(EntityLink::getTargetId)
                .distinct()
                .forEach(conceptId -> conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                        .ifPresent(concept -> {
                            if (concept.getFirstSourceReference() != null
                                    && Objects.equals(concept.getFirstSourceReference().getId(), sourceReferenceId)) {
                                concept.setFirstSourceReference(null);
                            }
                            refreshMentionCount(concept);
                        }));
    }

    @Transactional(readOnly = true)
    public Concept getOwnedConcept(User user, Long id) {
        return conceptRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Concept not found."));
    }

    private Concept upsertConcept(
            User user,
            Book book,
            SourceReference sourceReference,
            String name,
            String description,
            Visibility visibility) {
        String cleanedName = name.trim();
        String slug = SlugUtils.slugify(cleanedName);
        Concept concept = conceptRepository.findByUserIdAndSlugAndArchivedFalse(user.getId(), slug)
                .orElseGet(() -> {
                    Concept created = new Concept();
                    created.setUser(user);
                    created.setName(cleanedName);
                    created.setSlug(slug);
                    created.setVisibility(visibility == null ? Visibility.PRIVATE : visibility);
                    return created;
                });

        concept.setName(cleanedName);
        if (StringUtils.hasText(description)) {
            concept.setDescription(description.trim());
        }
        if (visibility != null) {
            concept.setVisibility(visibility);
        }
        if (concept.getFirstBook() == null) {
            concept.setFirstBook(book);
        }
        if (concept.getFirstSourceReference() == null) {
            concept.setFirstSourceReference(sourceReference);
        }
        return conceptRepository.save(concept);
    }

    private void linkSourceToConcept(User user, SourceReference sourceReference, Concept concept) {
        if (sourceReference == null) {
            return;
        }
        entityLinkRepository.findByUserIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetIdAndRelationType(
                        user.getId(),
                        ENTITY_TYPE_SOURCE_REFERENCE,
                        sourceReference.getId(),
                        ENTITY_TYPE_CONCEPT,
                        concept.getId(),
                        RELATION_MENTIONS_CONCEPT)
                .orElseGet(() -> {
                    EntityLink link = new EntityLink();
                    link.setUser(user);
                    link.setSourceType(ENTITY_TYPE_SOURCE_REFERENCE);
                    link.setSourceId(sourceReference.getId());
                    link.setTargetType(ENTITY_TYPE_CONCEPT);
                    link.setTargetId(concept.getId());
                    link.setRelationType(RELATION_MENTIONS_CONCEPT);
                    link.setSourceReferenceId(sourceReference.getId());
                    return entityLinkRepository.save(link);
                });
        refreshMentionCount(concept);
    }

    private void refreshMentionCount(Concept concept) {
        long count = entityLinkRepository.countByUserIdAndTargetTypeAndTargetIdAndRelationType(
                concept.getUser().getId(), ENTITY_TYPE_CONCEPT, concept.getId(), RELATION_MENTIONS_CONCEPT);
        concept.setMentionCount((int) count);
        conceptRepository.save(concept);
    }

    private ConceptResponse toResponse(User user, Concept concept) {
        SourceReferenceResponse firstSource = concept.getFirstSourceReference() == null
                ? null
                : toSourceResponse(concept.getFirstSourceReference());
        return new ConceptResponse(
                concept.getId(),
                concept.getName(),
                concept.getSlug(),
                concept.getDescription(),
                concept.getVisibility(),
                concept.getFirstBook() == null ? null : concept.getFirstBook().getId(),
                concept.getFirstBook() == null ? null : concept.getFirstBook().getTitle(),
                concept.getMentionCount(),
                firstSource,
                sourceReferencesForConcept(user, concept),
                concept.getCreatedAt(),
                concept.getUpdatedAt());
    }

    private List<SourceReferenceResponse> sourceReferencesForConcept(User user, Concept concept) {
        return entityLinkRepository.findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(
                        user.getId(), ENTITY_TYPE_CONCEPT, concept.getId())
                .stream()
                .filter(link -> RELATION_MENTIONS_CONCEPT.equals(link.getRelationType()))
                .filter(link -> ENTITY_TYPE_SOURCE_REFERENCE.equals(link.getSourceType()))
                .map(EntityLink::getSourceId)
                .distinct()
                .limit(20)
                .map(sourceId -> sourceReferenceRepository.findByIdAndUserId(sourceId, user.getId()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toSourceResponse)
                .toList();
    }

    private SourceReferenceResponse toSourceResponse(SourceReference sourceReference) {
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

    private Book getLibraryBook(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Add this book to your library before linking concepts.");
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

    private List<String> dedupe(List<String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        if (values == null) {
            return List.of();
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                String cleaned = value.trim();
                result.putIfAbsent(cleaned.toLowerCase(Locale.ROOT), cleaned);
            }
        }
        return List.copyOf(result.values());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
