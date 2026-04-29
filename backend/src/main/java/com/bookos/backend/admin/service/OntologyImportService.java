package com.bookos.backend.admin.service;

import com.bookos.backend.admin.dto.OntologyImportRequest;
import com.bookos.backend.admin.dto.OntologyImportResponse;
import com.bookos.backend.admin.dto.OntologySeedBookRequest;
import com.bookos.backend.admin.dto.OntologySeedConceptRequest;
import com.bookos.backend.admin.dto.OntologySeedKnowledgeObjectRequest;
import com.bookos.backend.book.entity.Author;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.BookAuthor;
import com.bookos.backend.book.entity.BookTag;
import com.bookos.backend.book.entity.Tag;
import com.bookos.backend.book.repository.AuthorRepository;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.TagRepository;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.config.SeedProperties;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OntologyImportService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private static final String SYSTEM_CREATOR = "SYSTEM";
    private static final String ONTOLOGY_SOURCE_TYPE = "ONTOLOGY_SEED";

    private final SeedProperties seedProperties;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final ObjectMapper objectMapper;

    public OntologyImportRequest defaultSeed() {
        return DefaultGameDesignOntology.request();
    }

    @Transactional
    public OntologyImportResponse importDefault(String email, boolean dryRun) {
        return importSeed(email, defaultSeed(), dryRun);
    }

    @Transactional
    public OntologyImportResponse importSeed(String email, OntologyImportRequest request, boolean dryRun) {
        if (!seedProperties.isEnabled()) {
            throw new AccessDeniedException("Ontology import is enabled only when APP_SEED_ENABLED=true.");
        }

        User user = userService.getByEmailRequired(email);
        ImportStats stats = new ImportStats(dryRun);
        OntologyImportRequest payload = request == null ? new OntologyImportRequest(List.of(), List.of(), List.of()) : request;
        validatePayload(payload, stats);

        for (OntologySeedBookRequest book : safeList(payload.books())) {
            ensureBook(user, book, dryRun, stats);
        }
        for (OntologySeedConceptRequest concept : safeList(payload.concepts())) {
            ensureConcept(user, concept, dryRun, stats);
        }
        for (OntologySeedKnowledgeObjectRequest object : safeList(payload.knowledgeObjects())) {
            ensureKnowledgeObject(user, object, dryRun, stats);
        }

        return stats.toResponse();
    }

    private Book ensureBook(User user, OntologySeedBookRequest request, boolean dryRun, ImportStats stats) {
        Book existing = bookRepository.findByTitleIgnoreCase(request.title().trim()).orElse(null);
        if (existing != null) {
            stats.booksExisting++;
            return existing;
        }
        if (dryRun) {
            stats.booksCreated++;
            return null;
        }

        Book book = new Book();
        book.setOwner(user);
        book.setTitle(request.title().trim());
        book.setSubtitle(trimToNull(request.subtitle()));
        book.setDescription(trimToNull(request.summary()));
        book.setPublisher(trimToNull(request.publisher()));
        book.setPublicationYear(request.publicationYear());
        book.setCategory(trimToNull(request.category()));
        book.setVisibility(request.visibility() == null ? Visibility.PUBLIC : request.visibility());
        replaceAuthors(book, request.authors());
        replaceTags(book, request.tags());
        stats.booksCreated++;
        return bookRepository.save(book);
    }

    private Concept ensureConcept(User user, OntologySeedConceptRequest request, boolean dryRun, ImportStats stats) {
        String title = request.title().trim();
        String slug = SlugUtils.slugify(title);
        Concept concept = conceptRepository.findByUserIdAndSlugAndArchivedFalse(user.getId(), slug).orElse(null);
        Book sourceBook = resolveSourceBook(request.sourceBookTitle(), dryRun, stats);
        SourceConfidence confidence = safeConfidence(request.sourceConfidence(), request.sourceBookTitle(), null, null, stats);
        boolean wasNew = concept == null;

        if (wasNew) {
            if (dryRun) {
                stats.conceptsCreated++;
                return null;
            }
            concept = new Concept();
            concept.setUser(user);
            concept.setName(title);
            concept.setSlug(slug);
            concept.setVisibility(Visibility.PUBLIC);
            concept.setCreatedBy(SYSTEM_CREATOR);
            stats.conceptsCreated++;
        }

        boolean changed = applyConceptSeed(concept, request, sourceBook, confidence);
        if (!dryRun) {
            if (sourceBook != null && concept.getFirstSourceReference() == null) {
                SourceReference sourceReference = createSeedSource(user, sourceBook, request.description(), null, null, confidence);
                concept.setFirstSourceReference(sourceReference);
                concept.setFirstBook(sourceBook);
                stats.sourceReferencesCreated++;
            }
            Concept saved = conceptRepository.save(concept);
            if (saved.getFirstSourceReference() != null) {
                link(saved.getUser(), "SOURCE_REFERENCE", saved.getFirstSourceReference().getId(), "CONCEPT", saved.getId(), "MENTIONS", saved.getFirstSourceReference().getId());
                saved.setMentionCount((int) entityLinkRepository.countByUserIdAndTargetTypeAndTargetIdAndRelationType(
                        saved.getUser().getId(), "CONCEPT", saved.getId(), "MENTIONS"));
                conceptRepository.save(saved);
            }
        }
        if (!wasNew && changed) {
            stats.conceptsUpdated++;
        } else if (!wasNew) {
            stats.conceptsExisting++;
        }
        return concept;
    }

    private KnowledgeObject ensureKnowledgeObject(
            User user,
            OntologySeedKnowledgeObjectRequest request,
            boolean dryRun,
            ImportStats stats) {
        String title = request.title().trim();
        String slug = SlugUtils.slugify(title);
        KnowledgeObject object = knowledgeObjectRepository.findByUserIdAndTypeAndSlugAndArchivedFalse(
                        user.getId(), request.type(), slug)
                .orElse(null);
        Book sourceBook = resolveSourceBook(request.sourceBookTitle(), dryRun, stats);
        Concept concept = resolveConcept(user, request.conceptTitle());
        SourceConfidence confidence = safeConfidence(
                request.sourceConfidence(), request.sourceBookTitle(), request.pageStart(), request.pageEnd(), stats);
        boolean wasNew = object == null;

        if (wasNew) {
            if (dryRun) {
                stats.knowledgeObjectsCreated++;
                return null;
            }
            object = new KnowledgeObject();
            object.setUser(user);
            object.setType(request.type());
            object.setTitle(title);
            object.setSlug(slug);
            object.setVisibility(Visibility.PUBLIC);
            object.setCreatedBy(SYSTEM_CREATOR);
            stats.knowledgeObjectsCreated++;
        }

        boolean changed = applyKnowledgeSeed(object, request, sourceBook, concept, confidence);
        if (!dryRun) {
            if (sourceBook != null && object.getSourceReferenceId() == null) {
                SourceReference sourceReference = createSeedSource(
                        user,
                        sourceBook,
                        request.description(),
                        request.pageStart(),
                        request.pageEnd(),
                        confidence);
                object.setSourceReferenceId(sourceReference.getId());
                stats.sourceReferencesCreated++;
            }
            KnowledgeObject saved = knowledgeObjectRepository.save(object);
            if (saved.getConcept() != null) {
                link(user, "CONCEPT", saved.getConcept().getId(), "KNOWLEDGE_OBJECT", saved.getId(), "RELATED_TO", saved.getSourceReferenceId());
            }
            if (saved.getSourceReferenceId() != null) {
                link(user, "SOURCE_REFERENCE", saved.getSourceReferenceId(), "KNOWLEDGE_OBJECT", saved.getId(), "DERIVED_FROM", saved.getSourceReferenceId());
            }
        }
        if (!wasNew && changed) {
            stats.knowledgeObjectsUpdated++;
        } else if (!wasNew) {
            stats.knowledgeObjectsExisting++;
        }
        return object;
    }

    private boolean applyConceptSeed(
            Concept concept,
            OntologySeedConceptRequest request,
            Book sourceBook,
            SourceConfidence confidence) {
        boolean changed = false;
        changed |= setIfDifferent(() -> concept.getDescription(), concept::setDescription, trimToNull(request.description()));
        changed |= setIfDifferent(() -> concept.getOntologyLayer(), concept::setOntologyLayer, request.layer().trim());
        changed |= setIfDifferent(() -> concept.getSourceConfidence(), concept::setSourceConfidence, confidence);
        changed |= setIfDifferent(() -> concept.getTagsJson(), concept::setTagsJson, writeList(cleanTags(request.tags())));
        changed |= setIfDifferent(() -> concept.getCreatedBy(), concept::setCreatedBy, SYSTEM_CREATOR);
        if (concept.getFirstBook() == null && sourceBook != null) {
            concept.setFirstBook(sourceBook);
            changed = true;
        }
        return changed;
    }

    private boolean applyKnowledgeSeed(
            KnowledgeObject object,
            OntologySeedKnowledgeObjectRequest request,
            Book sourceBook,
            Concept concept,
            SourceConfidence confidence) {
        boolean changed = false;
        changed |= setIfDifferent(() -> object.getDescription(), object::setDescription, trimToNull(request.description()));
        changed |= setIfDifferent(() -> object.getOntologyLayer(), object::setOntologyLayer, request.layer().trim());
        changed |= setIfDifferent(() -> object.getSourceConfidence(), object::setSourceConfidence, confidence);
        changed |= setIfDifferent(() -> object.getTagsJson(), object::setTagsJson, writeList(cleanTags(request.tags())));
        changed |= setIfDifferent(() -> object.getCreatedBy(), object::setCreatedBy, SYSTEM_CREATOR);
        if (object.getBook() == null && sourceBook != null) {
            object.setBook(sourceBook);
            changed = true;
        }
        if (object.getConcept() == null && concept != null) {
            object.setConcept(concept);
            changed = true;
        }
        return changed;
    }

    private SourceReference createSeedSource(
            User user,
            Book book,
            String sourceText,
            Integer pageStart,
            Integer pageEnd,
            SourceConfidence confidence) {
        SourceReference sourceReference = new SourceReference();
        sourceReference.setUser(user);
        sourceReference.setBook(book);
        sourceReference.setSourceType(ONTOLOGY_SOURCE_TYPE);
        sourceReference.setPageStart(pageStart);
        sourceReference.setPageEnd(pageEnd);
        sourceReference.setLocationLabel("Ontology seed: " + book.getTitle());
        sourceReference.setSourceText(trimToNull(sourceText));
        sourceReference.setSourceConfidence(confidence == null ? SourceConfidence.LOW : confidence);
        return sourceReferenceRepository.save(sourceReference);
    }

    private void replaceAuthors(Book book, List<String> authorNames) {
        book.getBookAuthors().clear();
        int index = 0;
        for (String authorName : cleanDisplayValues(authorNames)) {
            Author author = authorRepository.findByNameIgnoreCase(authorName).orElseGet(() -> {
                Author created = new Author();
                created.setName(authorName);
                created.setSlug(SlugUtils.slugify(authorName));
                return authorRepository.save(created);
            });
            BookAuthor link = new BookAuthor();
            link.setBook(book);
            link.setAuthor(author);
            link.setDisplayOrder(index++);
            book.getBookAuthors().add(link);
        }
    }

    private void replaceTags(Book book, List<String> tagNames) {
        book.getBookTags().clear();
        for (String tagName : cleanDisplayValues(tagNames)) {
            String slug = SlugUtils.slugify(tagName);
            Tag tag = tagRepository.findBySlugIgnoreCase(slug).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(tagName);
                created.setSlug(slug);
                return tagRepository.save(created);
            });
            BookTag link = new BookTag();
            link.setBook(book);
            link.setTag(tag);
            book.getBookTags().add(link);
        }
    }

    private Book resolveSourceBook(String sourceBookTitle, boolean dryRun, ImportStats stats) {
        if (!StringUtils.hasText(sourceBookTitle)) {
            return null;
        }
        Book existing = bookRepository.findByTitleIgnoreCase(sourceBookTitle.trim()).orElse(null);
        if (existing != null || dryRun) {
            return existing;
        }
        stats.warnings.add("Source book was not found and was not created: " + sourceBookTitle.trim());
        return null;
    }

    private Concept resolveConcept(User user, String conceptTitle) {
        if (!StringUtils.hasText(conceptTitle)) {
            return null;
        }
        return conceptRepository.findByUserIdAndSlugAndArchivedFalse(user.getId(), SlugUtils.slugify(conceptTitle.trim()))
                .orElse(null);
    }

    private SourceConfidence safeConfidence(
            SourceConfidence requested,
            String sourceBookTitle,
            Integer pageStart,
            Integer pageEnd,
            ImportStats stats) {
        SourceConfidence confidence = requested == null
                ? (StringUtils.hasText(sourceBookTitle) ? SourceConfidence.MEDIUM : SourceConfidence.LOW)
                : requested;
        if (confidence == SourceConfidence.HIGH && pageStart == null) {
            stats.warnings.add("HIGH source confidence requires a known page. Downgraded to MEDIUM.");
            return SourceConfidence.MEDIUM;
        }
        return confidence;
    }

    private void validatePayload(OntologyImportRequest payload, ImportStats stats) {
        Set<String> conceptSlugs = new LinkedHashSet<>();
        for (OntologySeedConceptRequest concept : safeList(payload.concepts())) {
            String slug = SlugUtils.slugify(concept.title().trim());
            if (!conceptSlugs.add(slug)) {
                throw new IllegalArgumentException("Duplicate concept in import payload: " + concept.title());
            }
        }

        Set<String> knowledgeKeys = new LinkedHashSet<>();
        for (OntologySeedKnowledgeObjectRequest object : safeList(payload.knowledgeObjects())) {
            if (object.pageStart() != null && object.pageEnd() != null && object.pageEnd() < object.pageStart()) {
                throw new IllegalArgumentException("Page end cannot be before page start for " + object.title());
            }
            if ((object.pageStart() != null || object.pageEnd() != null) && !StringUtils.hasText(object.sourceBookTitle())) {
                stats.warnings.add("Page metadata ignored without a source book: " + object.title());
            }
            String key = object.type().name() + ":" + SlugUtils.slugify(object.title().trim());
            if (!knowledgeKeys.add(key)) {
                throw new IllegalArgumentException("Duplicate knowledge object in import payload: " + object.title());
            }
        }
    }

    private void link(User user, String sourceType, Long sourceId, String targetType, Long targetId, String relationType, Long sourceReferenceId) {
        if (sourceId == null || targetId == null) {
            return;
        }
        entityLinkRepository.findByUserIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetIdAndRelationType(
                        user.getId(), sourceType, sourceId, targetType, targetId, relationType)
                .orElseGet(() -> {
                    EntityLink link = new EntityLink();
                    link.setUser(user);
                    link.setSourceType(sourceType);
                    link.setSourceId(sourceId);
                    link.setTargetType(targetType);
                    link.setTargetId(targetId);
                    link.setRelationType(relationType);
                    link.setSourceReferenceId(sourceReferenceId);
                    return entityLinkRepository.save(link);
                });
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private List<String> cleanDisplayValues(List<String> values) {
        List<String> cleaned = new ArrayList<>();
        if (values == null) {
            return cleaned;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)
                    && cleaned.stream().noneMatch(existing -> existing.equalsIgnoreCase(value.trim()))) {
                cleaned.add(value.trim());
            }
        }
        return cleaned;
    }

    private List<String> cleanTags(List<String> tags) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (tags == null) {
            return List.of();
        }
        for (String tag : tags) {
            if (StringUtils.hasText(tag)) {
                values.add(tag.trim().replaceFirst("^#", "").toLowerCase(Locale.ROOT));
            }
        }
        return List.copyOf(values);
    }

    private String writeList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize ontology tags.", exception);
        }
    }

    @SuppressWarnings("unused")
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

    private <T> boolean setIfDifferent(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter, T value) {
        if (!Objects.equals(getter.get(), value)) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private static final class ImportStats {
        private final boolean dryRun;
        private int booksCreated;
        private int booksExisting;
        private int conceptsCreated;
        private int conceptsUpdated;
        private int conceptsExisting;
        private int knowledgeObjectsCreated;
        private int knowledgeObjectsUpdated;
        private int knowledgeObjectsExisting;
        private int sourceReferencesCreated;
        private final List<String> warnings = new ArrayList<>();

        private ImportStats(boolean dryRun) {
            this.dryRun = dryRun;
        }

        private OntologyImportResponse toResponse() {
            return new OntologyImportResponse(
                    dryRun,
                    booksCreated,
                    booksExisting,
                    conceptsCreated,
                    conceptsUpdated,
                    conceptsExisting,
                    knowledgeObjectsCreated,
                    knowledgeObjectsUpdated,
                    knowledgeObjectsExisting,
                    sourceReferencesCreated,
                    List.copyOf(warnings));
        }
    }
}
