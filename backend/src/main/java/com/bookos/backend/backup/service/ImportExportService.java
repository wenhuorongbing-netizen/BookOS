package com.bookos.backend.backup.service;

import com.bookos.backend.action.dto.ActionItemRequest;
import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.action.service.ActionItemService;
import com.bookos.backend.backup.dto.ImportCommitResponse;
import com.bookos.backend.backup.dto.ImportPreviewResponse;
import com.bookos.backend.backup.dto.ImportRecordPreview;
import com.bookos.backend.backup.dto.ImportRequest;
import com.bookos.backend.book.dto.AddToLibraryRequest;
import com.bookos.backend.book.dto.BookRequest;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.BookAuthor;
import com.bookos.backend.book.entity.BookTag;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.book.service.UserLibraryService;
import com.bookos.backend.capture.dto.RawCaptureRequest;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.capture.service.RawCaptureService;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.daily.entity.DailyReflection;
import com.bookos.backend.daily.repository.DailyReflectionRepository;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.dto.ConceptRequest;
import com.bookos.backend.knowledge.dto.KnowledgeObjectRequest;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.knowledge.service.ConceptService;
import com.bookos.backend.knowledge.service.KnowledgeObjectService;
import com.bookos.backend.note.dto.BookNoteRequest;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.note.service.BookNoteService;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.quote.dto.QuoteRequest;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.quote.service.QuoteService;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ImportExportService {

    private static final String IMPORT_TYPE_JSON = "BOOKOS_JSON";
    private static final String IMPORT_TYPE_MARKDOWN = "MARKDOWN_NOTES";
    private static final String IMPORT_TYPE_QUOTES_CSV = "QUOTES_CSV";
    private static final String IMPORT_TYPE_ACTION_ITEMS_CSV = "ACTION_ITEMS_CSV";

    private final UserService userService;
    private final BookService bookService;
    private final UserLibraryService userLibraryService;
    private final BookNoteService bookNoteService;
    private final RawCaptureService rawCaptureService;
    private final QuoteService quoteService;
    private final ActionItemService actionItemService;
    private final ConceptService conceptService;
    private final KnowledgeObjectService knowledgeObjectService;
    private final UserBookRepository userBookRepository;
    private final BookNoteRepository bookNoteRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final DailyReflectionRepository dailyReflectionRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final GameProjectRepository gameProjectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> exportAllJson(String email) {
        User user = userService.getByEmailRequired(email);
        return exportPackage(user, null);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportBookJson(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        assertBookInLibrary(user, bookId);
        return exportPackage(user, bookId);
    }

    @Transactional(readOnly = true)
    public String exportBookMarkdown(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        UserBook userBook = userBookRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new AccessDeniedException("Book is not in your library."));
        Book book = userBook.getBook();
        List<BookNote> notes = bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId());
        List<Quote> quotes = quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        List<ActionItem> actions = actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        List<Concept> concepts = conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        List<SourceReference> sources = sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, user.getId());

        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(escapeMarkdown(book.getTitle())).append("\n\n");
        appendMetadata(markdown, "Subtitle", book.getSubtitle());
        appendMetadata(markdown, "Authors", String.join(", ", authorNames(book)));
        appendMetadata(markdown, "Category", book.getCategory());
        appendMetadata(markdown, "Reading status", userBook.getReadingStatus().name());
        appendMetadata(markdown, "Progress", userBook.getProgressPercent() + "%");
        appendMetadata(markdown, "Rating", userBook.getRating() == null ? null : String.valueOf(userBook.getRating()));
        markdown.append("\n## Notes\n\n");
        if (notes.isEmpty()) {
            markdown.append("_No notes exported for this book._\n\n");
        }
        for (BookNote note : notes) {
            markdown.append("### ").append(escapeMarkdown(note.getTitle())).append("\n\n");
            if (StringUtils.hasText(note.getThreeSentenceSummary())) {
                markdown.append("> ").append(note.getThreeSentenceSummary().replace("\n", "\n> ")).append("\n\n");
            }
            markdown.append(note.getMarkdown()).append("\n\n");
            if (!note.getBlocks().isEmpty()) {
                markdown.append("#### Note Blocks\n\n");
                for (NoteBlock block : note.getBlocks()) {
                    markdown.append("- ")
                            .append(block.getBlockType())
                            .append(pageSuffix(block.getPageStart(), block.getPageEnd()))
                            .append(": ")
                            .append(block.getRawText().replace("\n", " "))
                            .append("\n");
                }
                markdown.append("\n");
            }
        }

        markdown.append("## Quotes\n\n");
        appendQuoteList(markdown, quotes);
        markdown.append("## Action Items\n\n");
        appendActionList(markdown, actions);
        markdown.append("## Concepts\n\n");
        appendConceptList(markdown, concepts);
        markdown.append("## Source References\n\n");
        appendSourceList(markdown, sources);
        markdown.append("## Backlinks Summary\n\n");
        markdown.append("- Notes: ").append(notes.size()).append("\n");
        markdown.append("- Quotes: ").append(quotes.size()).append("\n");
        markdown.append("- Action items: ").append(actions.size()).append("\n");
        markdown.append("- Concepts: ").append(concepts.size()).append("\n");
        markdown.append("- Source references: ").append(sources.size()).append("\n");
        return markdown.toString();
    }

    @Transactional(readOnly = true)
    public String exportQuotesCsv(String email) {
        User user = userService.getByEmailRequired(email);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "bookTitle", "text", "attribution", "pageStart", "pageEnd", "tags", "concepts", "sourceReferenceId", "createdAt", "updatedAt"));
        for (Quote quote : quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())) {
            rows.add(List.of(
                    text(quote.getId()),
                    text(quote.getBook().getTitle()),
                    text(quote.getText()),
                    text(quote.getAttribution()),
                    text(quote.getPageStart()),
                    text(quote.getPageEnd()),
                    text(quote.getTagsJson()),
                    text(quote.getConceptsJson()),
                    text(quote.getSourceReferenceId()),
                    text(quote.getCreatedAt()),
                    text(quote.getUpdatedAt())));
        }
        return csv(rows);
    }

    @Transactional(readOnly = true)
    public String exportActionItemsCsv(String email) {
        User user = userService.getByEmailRequired(email);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "bookTitle", "title", "description", "priority", "completed", "pageStart", "pageEnd", "sourceReferenceId", "createdAt", "updatedAt"));
        for (ActionItem item : actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())) {
            rows.add(List.of(
                    text(item.getId()),
                    text(item.getBook().getTitle()),
                    text(item.getTitle()),
                    text(item.getDescription()),
                    text(item.getPriority()),
                    text(item.getCompletedAt() != null),
                    text(item.getPageStart()),
                    text(item.getPageEnd()),
                    text(item.getSourceReferenceId()),
                    text(item.getCreatedAt()),
                    text(item.getUpdatedAt())));
        }
        return csv(rows);
    }

    @Transactional(readOnly = true)
    public String exportConceptsCsv(String email) {
        User user = userService.getByEmailRequired(email);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("id", "name", "description", "ontologyLayer", "sourceConfidence", "bookTitle", "tags", "mentionCount", "createdAt", "updatedAt"));
        for (Concept concept : conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())) {
            rows.add(List.of(
                    text(concept.getId()),
                    text(concept.getName()),
                    text(concept.getDescription()),
                    text(concept.getOntologyLayer()),
                    text(concept.getSourceConfidence()),
                    text(concept.getFirstBook() == null ? null : concept.getFirstBook().getTitle()),
                    text(concept.getTagsJson()),
                    text(concept.getMentionCount()),
                    text(concept.getCreatedAt()),
                    text(concept.getUpdatedAt())));
        }
        return csv(rows);
    }

    @Transactional(readOnly = true)
    public ImportPreviewResponse previewImport(String email, ImportRequest request) {
        User user = userService.getByEmailRequired(email);
        ImportPlan plan = buildPlan(user, request);
        return plan.toPreview();
    }

    @Transactional
    public ImportCommitResponse commitImport(String email, ImportRequest request) {
        User user = userService.getByEmailRequired(email);
        ImportPlan plan = buildPlan(user, request);
        CommitStats stats = new CommitStats(plan.warnings);
        Map<Long, Long> oldBookIdToNew = new LinkedHashMap<>();
        Map<Long, Long> oldSourceIdToNew = new LinkedHashMap<>();
        Map<String, Long> titleToBookId = existingBookTitleMap(user);

        for (ImportBookRecord record : plan.books) {
            Long existingId = titleToBookId.get(normalizeKey(record.title()));
            if (existingId != null) {
                oldBookIdToNew.put(record.oldId(), existingId);
                stats.duplicatesSkipped++;
                continue;
            }
            Long newBookId = createImportedBook(email, record);
            titleToBookId.put(normalizeKey(record.title()), newBookId);
            oldBookIdToNew.put(record.oldId(), newBookId);
            stats.booksCreated++;
        }

        for (ImportSourceReferenceRecord record : plan.sourceReferences) {
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            if (bookId == null) {
                stats.warnings.add("Skipped source reference without a resolvable book: " + record.title());
                continue;
            }
            SourceReference sourceReference = ensureImportedSourceReference(user, bookId, record);
            if (!record.duplicate(user, sourceReferenceRepository)) {
                stats.sourceReferencesCreated++;
            }
            if (record.oldId() != null) {
                oldSourceIdToNew.put(record.oldId(), sourceReference.getId());
            }
        }

        for (ImportNoteRecord record : plan.notes) {
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            if (bookId == null) {
                stats.warnings.add("Skipped note without a resolvable book: " + record.title());
                continue;
            }
            if (isDuplicateNote(user, bookId, record.title())) {
                stats.duplicatesSkipped++;
                continue;
            }
            bookNoteService.createNote(email, bookId, new BookNoteRequest(record.title(), record.markdown(), Visibility.PRIVATE, record.summary()));
            stats.notesCreated++;
        }

        for (ImportCaptureRecord record : plan.captures) {
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            if (bookId == null) {
                stats.warnings.add("Skipped capture without a resolvable book.");
                continue;
            }
            if (isDuplicateCapture(user, bookId, record.rawText())) {
                stats.duplicatesSkipped++;
                continue;
            }
            rawCaptureService.createCapture(email, new RawCaptureRequest(bookId, record.rawText()));
            stats.capturesCreated++;
        }

        for (ImportQuoteRecord record : plan.quotes) {
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            if (bookId == null) {
                stats.warnings.add("Skipped quote without a resolvable book.");
                continue;
            }
            if (isDuplicateQuote(user, bookId, record.text())) {
                stats.duplicatesSkipped++;
                continue;
            }
            Long sourceReferenceId = record.oldSourceReferenceId() == null ? null : oldSourceIdToNew.get(record.oldSourceReferenceId());
            quoteService.createQuote(email, new QuoteRequest(
                    bookId,
                    record.text(),
                    record.attribution(),
                    sourceReferenceId,
                    record.pageStart(),
                    record.pageEnd(),
                    record.tags(),
                    record.concepts(),
                    Visibility.PRIVATE));
            stats.quotesCreated++;
        }

        for (ImportActionItemRecord record : plan.actionItems) {
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            if (bookId == null) {
                stats.warnings.add("Skipped action item without a resolvable book: " + record.title());
                continue;
            }
            if (isDuplicateActionItem(user, bookId, record.title())) {
                stats.duplicatesSkipped++;
                continue;
            }
            Long sourceReferenceId = record.oldSourceReferenceId() == null ? null : oldSourceIdToNew.get(record.oldSourceReferenceId());
            actionItemService.createActionItem(email, new ActionItemRequest(
                    bookId,
                    record.title(),
                    record.description(),
                    record.priority(),
                    sourceReferenceId,
                    record.pageStart(),
                    record.pageEnd(),
                    Visibility.PRIVATE));
            stats.actionItemsCreated++;
        }

        for (ImportConceptRecord record : plan.concepts) {
            if (isDuplicateConcept(user, record.name())) {
                stats.duplicatesSkipped++;
                continue;
            }
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            Long sourceReferenceId = record.oldSourceReferenceId() == null ? null : oldSourceIdToNew.get(record.oldSourceReferenceId());
            conceptService.createConcept(email, new ConceptRequest(
                    record.name(),
                    record.description(),
                    Visibility.PRIVATE,
                    bookId,
                    sourceReferenceId,
                    record.ontologyLayer(),
                    record.tags()));
            stats.conceptsCreated++;
        }

        for (ImportKnowledgeObjectRecord record : plan.knowledgeObjects) {
            if (isDuplicateKnowledgeObject(user, record.type(), record.title())) {
                stats.duplicatesSkipped++;
                continue;
            }
            Long bookId = resolveMappedBookId(record.bookTitle(), record.oldBookId(), oldBookIdToNew, titleToBookId);
            Long sourceReferenceId = record.oldSourceReferenceId() == null ? null : oldSourceIdToNew.get(record.oldSourceReferenceId());
            knowledgeObjectService.createKnowledgeObject(email, new KnowledgeObjectRequest(
                    record.type(),
                    record.title(),
                    record.description(),
                    Visibility.PRIVATE,
                    bookId,
                    null,
                    null,
                    sourceReferenceId,
                    record.ontologyLayer(),
                    record.tags()));
            stats.knowledgeObjectsCreated++;
        }

        for (ImportProjectRecord record : plan.projects) {
            if (isDuplicateProject(user, record.title())) {
                stats.duplicatesSkipped++;
                continue;
            }
            GameProject project = new GameProject();
            project.setOwner(user);
            project.setTitle(record.title());
            project.setSlug(uniqueProjectSlug(user, record.title()));
            project.setDescription(record.description());
            project.setGenre(record.genre());
            project.setPlatform(record.platform());
            project.setStage(StringUtils.hasText(record.stage()) ? record.stage() : "IDEATION");
            project.setVisibility(Visibility.PRIVATE);
            project.setProgressPercent(clamp(record.progressPercent(), 0, 100));
            gameProjectRepository.save(project);
            stats.projectsCreated++;
        }

        return stats.toResponse();
    }

    private Map<String, Object> exportPackage(User user, Long bookId) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", "bookos.backup.v1");
        root.put("exportedAt", Instant.now());
        root.put("user", Map.of("id", user.getId(), "email", user.getEmail()));

        List<UserBook> library = userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(userBook -> bookId == null || Objects.equals(userBook.getBook().getId(), bookId))
                .toList();
        Set<Long> exportedBookIds = new LinkedHashSet<>();
        library.forEach(userBook -> exportedBookIds.add(userBook.getBook().getId()));

        root.put("books", library.stream().map(this::bookRow).toList());
        root.put("notes", bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(note -> bookId == null || Objects.equals(note.getBook().getId(), bookId))
                .map(this::noteRow)
                .toList());
        root.put("captures", rawCaptureRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(capture -> bookId == null || Objects.equals(capture.getBook().getId(), bookId))
                .map(this::captureRow)
                .toList());
        root.put("quotes", quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(quote -> bookId == null || Objects.equals(quote.getBook().getId(), bookId))
                .map(this::quoteRow)
                .toList());
        root.put("actionItems", actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(item -> bookId == null || Objects.equals(item.getBook().getId(), bookId))
                .map(this::actionItemRow)
                .toList());
        root.put("concepts", conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(concept -> bookId == null || concept.getFirstBook() != null && Objects.equals(concept.getFirstBook().getId(), bookId))
                .map(this::conceptRow)
                .toList());
        root.put("knowledgeObjects", knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(item -> bookId == null || item.getBook() != null && Objects.equals(item.getBook().getId(), bookId))
                .map(this::knowledgeObjectRow)
                .toList());
        root.put("sourceReferences", sourceReferenceRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(source -> bookId == null || Objects.equals(source.getBook().getId(), bookId))
                .map(this::sourceReferenceRow)
                .toList());
        root.put("projects", bookId == null
                ? gameProjectRepository.findByOwnerIdAndArchivedAtIsNullOrderByUpdatedAtDesc(user.getId()).stream().map(this::projectRow).toList()
                : List.of());
        root.put("projectApplications", bookId == null
                ? projectApplicationRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).stream().map(this::projectApplicationRow).toList()
                : List.of());
        root.put("forumThreads", forumThreadRepository.findByAuthorIdAndStatusNotOrderByUpdatedAtDesc(user.getId(), com.bookos.backend.common.enums.ForumThreadStatus.ARCHIVED)
                .stream()
                .filter(thread -> bookId == null || thread.getRelatedBook() != null && exportedBookIds.contains(thread.getRelatedBook().getId()))
                .map(this::forumThreadRow)
                .toList());
        root.put("dailyReflections", dailyReflectionRepository.findTop30ByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::dailyReflectionRow)
                .toList());
        return root;
    }

    private ImportPlan buildPlan(User user, ImportRequest request) {
        String type = normalizeImportType(request.importType());
        ImportPlan plan = new ImportPlan(type);
        switch (type) {
            case IMPORT_TYPE_JSON -> parseJsonBackup(request.content(), plan);
            case IMPORT_TYPE_MARKDOWN -> parseMarkdownNotes(request, plan);
            case IMPORT_TYPE_QUOTES_CSV -> parseQuotesCsv(request.content(), plan);
            case IMPORT_TYPE_ACTION_ITEMS_CSV -> parseActionItemsCsv(request.content(), plan);
            default -> throw new IllegalArgumentException("Unsupported import type: " + request.importType());
        }
        markDuplicates(user, plan);
        return plan;
    }

    private void parseJsonBackup(String content, ImportPlan plan) {
        JsonNode root = readJson(content);
        if (!"bookos.backup.v1".equals(root.path("schemaVersion").asText())) {
            plan.warnings.add("JSON backup schema is not bookos.backup.v1; unsupported fields will be ignored.");
        }
        for (JsonNode node : array(root, "books")) {
            plan.books.add(new ImportBookRecord(
                    longOrNull(node, "id"),
                    requiredText(node, "title", "Untitled imported book"),
                    textOrNull(node, "subtitle"),
                    textOrNull(node, "description"),
                    textOrNull(node, "isbn"),
                    textOrNull(node, "publisher"),
                    intOrNull(node, "publicationYear"),
                    textOrNull(node, "coverUrl"),
                    textOrNull(node, "category"),
                    strings(node, "authors"),
                    strings(node, "tags")));
        }
        for (JsonNode node : array(root, "sourceReferences")) {
            plan.sourceReferences.add(sourceReferenceRecord(node, plan));
        }
        for (JsonNode node : array(root, "notes")) {
            plan.notes.add(new ImportNoteRecord(
                    longOrNull(node, "id"),
                    longOrNull(node, "bookId"),
                    textOrNull(node, "bookTitle"),
                    requiredText(node, "title", "Imported note"),
                    requiredText(node, "markdown", ""),
                    textOrNull(node, "threeSentenceSummary")));
        }
        for (JsonNode node : array(root, "captures")) {
            plan.captures.add(new ImportCaptureRecord(
                    longOrNull(node, "id"),
                    longOrNull(node, "bookId"),
                    textOrNull(node, "bookTitle"),
                    requiredText(node, "rawText", "")));
        }
        for (JsonNode node : array(root, "quotes")) {
            plan.quotes.add(new ImportQuoteRecord(
                    longOrNull(node, "id"),
                    longOrNull(node, "bookId"),
                    textOrNull(node, "bookTitle"),
                    requiredText(node, "text", ""),
                    textOrNull(node, "attribution"),
                    longOrNull(node, "sourceReferenceId"),
                    page(node, "pageStart", plan),
                    page(node, "pageEnd", plan),
                    strings(node, "tags"),
                    strings(node, "concepts")));
        }
        for (JsonNode node : array(root, "actionItems")) {
            plan.actionItems.add(new ImportActionItemRecord(
                    longOrNull(node, "id"),
                    longOrNull(node, "bookId"),
                    textOrNull(node, "bookTitle"),
                    requiredText(node, "title", "Imported action item"),
                    textOrNull(node, "description"),
                    longOrNull(node, "sourceReferenceId"),
                    priority(textOrNull(node, "priority")),
                    page(node, "pageStart", plan),
                    page(node, "pageEnd", plan)));
        }
        for (JsonNode node : array(root, "concepts")) {
            plan.concepts.add(new ImportConceptRecord(
                    longOrNull(node, "id"),
                    longOrNull(node, "bookId"),
                    textOrNull(node, "bookTitle"),
                    requiredText(node, "name", "Imported concept"),
                    textOrNull(node, "description"),
                    textOrNull(node, "ontologyLayer"),
                    longOrNull(node, "sourceReferenceId"),
                    strings(node, "tags")));
        }
        for (JsonNode node : array(root, "knowledgeObjects")) {
            KnowledgeObjectType objectType = knowledgeType(textOrNull(node, "type"), plan);
            if (objectType != null) {
                plan.knowledgeObjects.add(new ImportKnowledgeObjectRecord(
                        longOrNull(node, "id"),
                        longOrNull(node, "bookId"),
                        textOrNull(node, "bookTitle"),
                        objectType,
                        requiredText(node, "title", "Imported knowledge object"),
                        textOrNull(node, "description"),
                        textOrNull(node, "ontologyLayer"),
                        longOrNull(node, "sourceReferenceId"),
                        strings(node, "tags")));
            }
        }
        for (JsonNode node : array(root, "projects")) {
            plan.projects.add(new ImportProjectRecord(
                    longOrNull(node, "id"),
                    requiredText(node, "title", "Imported project"),
                    textOrNull(node, "description"),
                    textOrNull(node, "genre"),
                    textOrNull(node, "platform"),
                    textOrNull(node, "stage"),
                    clamp(intOrDefault(node, "progressPercent", 0), 0, 100)));
        }
    }

    private void parseMarkdownNotes(ImportRequest request, ImportPlan plan) {
        String title = firstMarkdownHeading(request.content()).orElse(trimToNull(request.bookTitle()));
        if (!StringUtils.hasText(title)) {
            title = stripExtension(request.fileName());
        }
        if (!StringUtils.hasText(title)) {
            title = "Imported Markdown Book";
        }
        ImportBookRecord book = new ImportBookRecord(null, title, null, null, null, null, null, null, "Imported Notes", List.of(), List.of("imported", "markdown"));
        plan.books.add(book);
        plan.notes.add(new ImportNoteRecord(null, null, book.title(), title + " Notes", request.content(), null));
        collectPageWarnings(request.content(), plan);
    }

    private void parseQuotesCsv(String content, ImportPlan plan) {
        for (Map<String, String> row : csvRows(content)) {
            String bookTitle = firstNonBlank(row.get("bookTitle"), row.get("book"), row.get("title_book"));
            String text = firstNonBlank(row.get("text"), row.get("quote"), row.get("content"));
            if (!StringUtils.hasText(bookTitle) || !StringUtils.hasText(text)) {
                plan.warnings.add("Skipped quote CSV row missing bookTitle or text.");
                continue;
            }
            plan.books.add(new ImportBookRecord(null, bookTitle, null, null, null, null, null, null, "Imported Quotes", List.of(), List.of("imported")));
            plan.quotes.add(new ImportQuoteRecord(
                    null,
                    null,
                    bookTitle,
                    text,
                    row.get("attribution"),
                    null,
                    parsePage(row.get("pageStart"), plan),
                    parsePage(row.get("pageEnd"), plan),
                    splitList(row.get("tags")),
                    splitList(row.get("concepts"))));
        }
    }

    private void parseActionItemsCsv(String content, ImportPlan plan) {
        for (Map<String, String> row : csvRows(content)) {
            String bookTitle = firstNonBlank(row.get("bookTitle"), row.get("book"));
            String title = firstNonBlank(row.get("title"), row.get("action"), row.get("text"));
            if (!StringUtils.hasText(bookTitle) || !StringUtils.hasText(title)) {
                plan.warnings.add("Skipped action item CSV row missing bookTitle or title.");
                continue;
            }
            plan.books.add(new ImportBookRecord(null, bookTitle, null, null, null, null, null, null, "Imported Actions", List.of(), List.of("imported")));
            plan.actionItems.add(new ImportActionItemRecord(
                    null,
                    null,
                    bookTitle,
                    title,
                    row.get("description"),
                    null,
                    priority(row.get("priority")),
                    parsePage(row.get("pageStart"), plan),
                    parsePage(row.get("pageEnd"), plan)));
        }
    }

    private void markDuplicates(User user, ImportPlan plan) {
        Map<String, Long> bookTitles = existingBookTitleMap(user);
        Set<String> plannedBooks = new LinkedHashSet<>();
        for (ImportBookRecord record : plan.books) {
            String key = normalizeKey(record.title());
            boolean duplicate = false;
            Long existingId = null;
            String action = "CREATE";
            if (!plannedBooks.add(key)) {
                duplicate = true;
                existingId = bookTitles.get(key);
                action = "MERGE_WITH_PLANNED_BOOK";
            } else if (bookTitles.containsKey(key)) {
                duplicate = true;
                existingId = bookTitles.get(key);
                action = "SKIP_DUPLICATE";
            }
            plan.records.add(new ImportRecordPreview("BOOK", record.title(), "book:" + normalizeKey(record.title()), duplicate, action, existingId));
        }
        plan.notes.forEach(record -> {
            Long bookId = bookTitles.get(normalizeKey(record.bookTitle()));
            boolean duplicate = bookId != null && isDuplicateNote(user, bookId, record.title());
            plan.records.add(new ImportRecordPreview(
                    "NOTE",
                    record.title(),
                    "note:" + normalize(record.title(), record.bookTitle()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.captures.forEach(record -> {
            Long bookId = bookTitles.get(normalizeKey(record.bookTitle()));
            boolean duplicate = bookId != null && isDuplicateCapture(user, bookId, record.rawText());
            plan.records.add(new ImportRecordPreview(
                    "CAPTURE",
                    previewText(record.rawText()),
                    "capture:" + normalize(record.rawText(), record.bookTitle()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.quotes.forEach(record -> {
            Long bookId = bookTitles.get(normalizeKey(record.bookTitle()));
            boolean duplicate = bookId != null && isDuplicateQuote(user, bookId, record.text());
            plan.records.add(new ImportRecordPreview(
                    "QUOTE",
                    previewText(record.text()),
                    "quote:" + normalize(record.text(), record.bookTitle()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.actionItems.forEach(record -> {
            Long bookId = bookTitles.get(normalizeKey(record.bookTitle()));
            boolean duplicate = bookId != null && isDuplicateActionItem(user, bookId, record.title());
            plan.records.add(new ImportRecordPreview(
                    "ACTION_ITEM",
                    record.title(),
                    "action:" + normalize(record.title(), record.bookTitle()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.concepts.forEach(record -> {
            boolean duplicate = isDuplicateConcept(user, record.name());
            plan.records.add(new ImportRecordPreview(
                    "CONCEPT",
                    record.name(),
                    "concept:" + normalizeKey(record.name()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.knowledgeObjects.forEach(record -> {
            boolean duplicate = isDuplicateKnowledgeObject(user, record.type(), record.title());
            plan.records.add(new ImportRecordPreview(
                    "KNOWLEDGE_OBJECT",
                    record.title(),
                    "knowledge:" + record.type() + ":" + normalizeKey(record.title()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.projects.forEach(record -> {
            boolean duplicate = isDuplicateProject(user, record.title());
            plan.records.add(new ImportRecordPreview(
                    "PROJECT",
                    record.title(),
                    "project:" + normalizeKey(record.title()),
                    duplicate,
                    duplicate ? "SKIP_DUPLICATE" : "CREATE",
                    null));
        });
        plan.sourceReferences.forEach(record -> plan.records.add(record.preview()));
    }

    private Long createImportedBook(String email, ImportBookRecord record) {
        var response = bookService.createBook(email, new BookRequest(
                record.title(),
                record.subtitle(),
                record.description(),
                record.isbn(),
                record.publisher(),
                record.publicationYear(),
                record.coverUrl(),
                record.category(),
                Visibility.PRIVATE,
                record.authors(),
                record.tags()));
        userLibraryService.addToLibrary(email, response.id(), new AddToLibraryRequest(null, null, null));
        return response.id();
    }

    private SourceReference ensureImportedSourceReference(User user, Long bookId, ImportSourceReferenceRecord record) {
        String key = record.stableKey(bookId);
        for (SourceReference existing : sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, user.getId())) {
            if (key.equals(record.stableKey(existing))) {
                return existing;
            }
        }
        Book book = bookService.getBookEntity(bookId);
        SourceReference source = new SourceReference();
        source.setUser(user);
        source.setBook(book);
        source.setSourceType(StringUtils.hasText(record.sourceType()) ? record.sourceType() : "IMPORTED");
        source.setPageStart(record.pageStart());
        source.setPageEnd(record.pageEnd());
        source.setLocationLabel(record.locationLabel());
        source.setSourceText(record.sourceText());
        source.setSourceConfidence(record.sourceConfidence() == null ? SourceConfidence.LOW : record.sourceConfidence());
        return sourceReferenceRepository.save(source);
    }

    private Map<String, Long> existingBookTitleMap(User user) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (UserBook userBook : userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())) {
            result.put(normalizeKey(userBook.getBook().getTitle()), userBook.getBook().getId());
        }
        return result;
    }

    private Long resolveMappedBookId(String bookTitle, Long oldBookId, Map<Long, Long> oldBookIdToNew, Map<String, Long> titleToBookId) {
        if (oldBookId != null && oldBookIdToNew.containsKey(oldBookId)) {
            return oldBookIdToNew.get(oldBookId);
        }
        return titleToBookId.get(normalizeKey(bookTitle));
    }

    private boolean isDuplicateNote(User user, Long bookId, String title) {
        return bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId())
                .stream()
                .anyMatch(note -> note.getTitle().equalsIgnoreCase(title));
    }

    private boolean isDuplicateCapture(User user, Long bookId, String rawText) {
        return rawCaptureRepository.findByUserIdAndBookIdOrderByCreatedAtDesc(user.getId(), bookId)
                .stream()
                .anyMatch(capture -> capture.getRawText().equals(rawText));
    }

    private boolean isDuplicateQuote(User user, Long bookId, String text) {
        return quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId)
                .stream()
                .anyMatch(quote -> quote.getText().equalsIgnoreCase(text));
    }

    private boolean isDuplicateActionItem(User user, Long bookId, String title) {
        return actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId)
                .stream()
                .anyMatch(item -> item.getTitle().equalsIgnoreCase(title));
    }

    private boolean isDuplicateConcept(User user, String name) {
        return conceptRepository.findByUserIdAndSlugAndArchivedFalse(user.getId(), SlugUtils.slugify(name)).isPresent();
    }

    private boolean isDuplicateKnowledgeObject(User user, KnowledgeObjectType type, String title) {
        return knowledgeObjectRepository.findByUserIdAndTypeAndSlugAndArchivedFalse(user.getId(), type, SlugUtils.slugify(title)).isPresent();
    }

    private boolean isDuplicateProject(User user, String title) {
        return gameProjectRepository.existsByOwnerIdAndSlug(user.getId(), SlugUtils.slugify(title));
    }

    private void assertBookInLibrary(User user, Long bookId) {
        if (userBookRepository.findByUserIdAndBookId(user.getId(), bookId).isEmpty()) {
            throw new AccessDeniedException("Book is not in your library.");
        }
    }

    private String uniqueProjectSlug(User user, String title) {
        String base = SlugUtils.slugify(title);
        String candidate = base;
        int index = 2;
        while (gameProjectRepository.existsByOwnerIdAndSlug(user.getId(), candidate)) {
            candidate = base + "-" + index++;
        }
        return candidate;
    }

    private Map<String, Object> bookRow(UserBook userBook) {
        Book book = userBook.getBook();
        Map<String, Object> row = baseRow(book.getId(), book.getCreatedAt(), book.getUpdatedAt());
        row.put("title", book.getTitle());
        row.put("subtitle", book.getSubtitle());
        row.put("description", book.getDescription());
        row.put("isbn", book.getIsbn());
        row.put("publisher", book.getPublisher());
        row.put("publicationYear", book.getPublicationYear());
        row.put("coverUrl", book.getCoverUrl());
        row.put("category", book.getCategory());
        row.put("authors", authorNames(book));
        row.put("tags", tagNames(book));
        row.put("readingStatus", userBook.getReadingStatus());
        row.put("readingFormat", userBook.getReadingFormat());
        row.put("ownershipStatus", userBook.getOwnershipStatus());
        row.put("progressPercent", userBook.getProgressPercent());
        row.put("rating", userBook.getRating());
        return row;
    }

    private Map<String, Object> noteRow(BookNote note) {
        Map<String, Object> row = baseRow(note.getId(), note.getCreatedAt(), note.getUpdatedAt());
        row.put("bookId", note.getBook().getId());
        row.put("bookTitle", note.getBook().getTitle());
        row.put("title", note.getTitle());
        row.put("markdown", note.getMarkdown());
        row.put("threeSentenceSummary", note.getThreeSentenceSummary());
        row.put("blocks", note.getBlocks().stream().map(this::noteBlockRow).toList());
        return row;
    }

    private Map<String, Object> noteBlockRow(NoteBlock block) {
        Map<String, Object> row = baseRow(block.getId(), block.getCreatedAt(), block.getUpdatedAt());
        row.put("blockType", block.getBlockType());
        row.put("rawText", block.getRawText());
        row.put("plainText", block.getPlainText());
        row.put("pageStart", block.getPageStart());
        row.put("pageEnd", block.getPageEnd());
        return row;
    }

    private Map<String, Object> captureRow(RawCapture capture) {
        Map<String, Object> row = baseRow(capture.getId(), capture.getCreatedAt(), capture.getUpdatedAt());
        row.put("bookId", capture.getBook().getId());
        row.put("bookTitle", capture.getBook().getTitle());
        row.put("rawText", capture.getRawText());
        row.put("cleanText", capture.getCleanText());
        row.put("parsedType", capture.getParsedType());
        row.put("pageStart", capture.getPageStart());
        row.put("pageEnd", capture.getPageEnd());
        row.put("tags", parseJsonList(capture.getTagsJson()));
        row.put("concepts", parseJsonList(capture.getConceptsJson()));
        row.put("status", capture.getStatus());
        return row;
    }

    private Map<String, Object> quoteRow(Quote quote) {
        Map<String, Object> row = baseRow(quote.getId(), quote.getCreatedAt(), quote.getUpdatedAt());
        row.put("bookId", quote.getBook().getId());
        row.put("bookTitle", quote.getBook().getTitle());
        row.put("text", quote.getText());
        row.put("attribution", quote.getAttribution());
        row.put("sourceReferenceId", quote.getSourceReferenceId());
        row.put("pageStart", quote.getPageStart());
        row.put("pageEnd", quote.getPageEnd());
        row.put("tags", parseJsonList(quote.getTagsJson()));
        row.put("concepts", parseJsonList(quote.getConceptsJson()));
        return row;
    }

    private Map<String, Object> actionItemRow(ActionItem item) {
        Map<String, Object> row = baseRow(item.getId(), item.getCreatedAt(), item.getUpdatedAt());
        row.put("bookId", item.getBook().getId());
        row.put("bookTitle", item.getBook().getTitle());
        row.put("title", item.getTitle());
        row.put("description", item.getDescription());
        row.put("priority", item.getPriority());
        row.put("completed", item.getCompletedAt() != null);
        row.put("sourceReferenceId", item.getSourceReferenceId());
        row.put("pageStart", item.getPageStart());
        row.put("pageEnd", item.getPageEnd());
        return row;
    }

    private Map<String, Object> conceptRow(Concept concept) {
        Map<String, Object> row = baseRow(concept.getId(), concept.getCreatedAt(), concept.getUpdatedAt());
        row.put("name", concept.getName());
        row.put("description", concept.getDescription());
        row.put("ontologyLayer", concept.getOntologyLayer());
        row.put("sourceConfidence", concept.getSourceConfidence());
        row.put("bookId", concept.getFirstBook() == null ? null : concept.getFirstBook().getId());
        row.put("bookTitle", concept.getFirstBook() == null ? null : concept.getFirstBook().getTitle());
        row.put("sourceReferenceId", concept.getFirstSourceReference() == null ? null : concept.getFirstSourceReference().getId());
        row.put("tags", parseJsonList(concept.getTagsJson()));
        row.put("mentionCount", concept.getMentionCount());
        return row;
    }

    private Map<String, Object> knowledgeObjectRow(KnowledgeObject item) {
        Map<String, Object> row = baseRow(item.getId(), item.getCreatedAt(), item.getUpdatedAt());
        row.put("type", item.getType());
        row.put("title", item.getTitle());
        row.put("description", item.getDescription());
        row.put("ontologyLayer", item.getOntologyLayer());
        row.put("sourceConfidence", item.getSourceConfidence());
        row.put("bookId", item.getBook() == null ? null : item.getBook().getId());
        row.put("bookTitle", item.getBook() == null ? null : item.getBook().getTitle());
        row.put("sourceReferenceId", item.getSourceReferenceId());
        row.put("tags", parseJsonList(item.getTagsJson()));
        return row;
    }

    private Map<String, Object> sourceReferenceRow(SourceReference source) {
        Map<String, Object> row = baseRow(source.getId(), source.getCreatedAt(), source.getUpdatedAt());
        row.put("bookId", source.getBook().getId());
        row.put("bookTitle", source.getBook().getTitle());
        row.put("sourceType", source.getSourceType());
        row.put("pageStart", source.getPageStart());
        row.put("pageEnd", source.getPageEnd());
        row.put("locationLabel", source.getLocationLabel());
        row.put("sourceText", source.getSourceText());
        row.put("sourceConfidence", source.getSourceConfidence());
        return row;
    }

    private Map<String, Object> projectRow(GameProject project) {
        Map<String, Object> row = baseRow(project.getId(), project.getCreatedAt(), project.getUpdatedAt());
        row.put("title", project.getTitle());
        row.put("description", project.getDescription());
        row.put("genre", project.getGenre());
        row.put("platform", project.getPlatform());
        row.put("stage", project.getStage());
        row.put("progressPercent", project.getProgressPercent());
        return row;
    }

    private Map<String, Object> projectApplicationRow(ProjectApplication application) {
        Map<String, Object> row = baseRow(application.getId(), application.getCreatedAt(), application.getUpdatedAt());
        row.put("projectId", application.getProject().getId());
        row.put("projectTitle", application.getProject().getTitle());
        row.put("title", application.getTitle());
        row.put("description", application.getDescription());
        row.put("applicationType", application.getApplicationType());
        row.put("sourceReferenceId", application.getSourceReference() == null ? null : application.getSourceReference().getId());
        return row;
    }

    private Map<String, Object> forumThreadRow(ForumThread thread) {
        Map<String, Object> row = baseRow(thread.getId(), thread.getCreatedAt(), thread.getUpdatedAt());
        row.put("title", thread.getTitle());
        row.put("bodyMarkdown", thread.getBodyMarkdown());
        row.put("category", thread.getCategory().getName());
        row.put("relatedEntityType", thread.getRelatedEntityType());
        row.put("relatedEntityId", thread.getRelatedEntityId());
        row.put("sourceReferenceId", thread.getSourceReferenceId());
        return row;
    }

    private Map<String, Object> dailyReflectionRow(DailyReflection reflection) {
        Map<String, Object> row = baseRow(reflection.getId(), reflection.getCreatedAt(), reflection.getUpdatedAt());
        row.put("day", reflection.getDay());
        row.put("targetType", reflection.getTargetType());
        row.put("reflectionText", reflection.getReflectionText());
        row.put("dailySentenceId", reflection.getDailySentenceId());
        row.put("dailyDesignPromptId", reflection.getDailyDesignPromptId());
        return row;
    }

    private Map<String, Object> baseRow(Long id, Instant createdAt, Instant updatedAt) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("createdAt", createdAt);
        row.put("updatedAt", updatedAt);
        return row;
    }

    private ImportSourceReferenceRecord sourceReferenceRecord(JsonNode node, ImportPlan plan) {
        SourceConfidence confidence = sourceConfidence(textOrNull(node, "sourceConfidence"), plan);
        return new ImportSourceReferenceRecord(
                longOrNull(node, "id"),
                longOrNull(node, "bookId"),
                textOrNull(node, "bookTitle"),
                textOrNull(node, "sourceType"),
                page(node, "pageStart", plan),
                page(node, "pageEnd", plan),
                textOrNull(node, "locationLabel"),
                textOrNull(node, "sourceText"),
                confidence == null ? SourceConfidence.LOW : confidence);
    }

    private void appendMetadata(StringBuilder markdown, String label, String value) {
        if (StringUtils.hasText(value)) {
            markdown.append("- **").append(label).append(":** ").append(value).append("\n");
        }
    }

    private void appendQuoteList(StringBuilder markdown, List<Quote> quotes) {
        if (quotes.isEmpty()) {
            markdown.append("_No quotes exported._\n\n");
            return;
        }
        for (Quote quote : quotes) {
            markdown.append("- ").append(pageSuffix(quote.getPageStart(), quote.getPageEnd())).append(" ")
                    .append(quote.getText().replace("\n", " "));
            if (StringUtils.hasText(quote.getAttribution())) {
                markdown.append(" — ").append(quote.getAttribution());
            }
            markdown.append("\n");
        }
        markdown.append("\n");
    }

    private void appendActionList(StringBuilder markdown, List<ActionItem> actions) {
        if (actions.isEmpty()) {
            markdown.append("_No action items exported._\n\n");
            return;
        }
        for (ActionItem action : actions) {
            markdown.append("- [").append(action.getCompletedAt() == null ? " " : "x").append("] ")
                    .append(action.getTitle())
                    .append(pageSuffix(action.getPageStart(), action.getPageEnd()))
                    .append(" (").append(action.getPriority()).append(")\n");
        }
        markdown.append("\n");
    }

    private void appendConceptList(StringBuilder markdown, List<Concept> concepts) {
        if (concepts.isEmpty()) {
            markdown.append("_No concepts exported._\n\n");
            return;
        }
        for (Concept concept : concepts) {
            markdown.append("- [[")
                    .append(concept.getName())
                    .append("]]");
            if (StringUtils.hasText(concept.getOntologyLayer())) {
                markdown.append(" — ").append(concept.getOntologyLayer());
            }
            markdown.append("\n");
        }
        markdown.append("\n");
    }

    private void appendSourceList(StringBuilder markdown, List<SourceReference> sources) {
        if (sources.isEmpty()) {
            markdown.append("_No source references exported._\n\n");
            return;
        }
        for (SourceReference source : sources) {
            markdown.append("- ")
                    .append(source.getSourceType())
                    .append(pageSuffix(source.getPageStart(), source.getPageEnd()))
                    .append(" — confidence: ")
                    .append(source.getSourceConfidence())
                    .append("\n");
        }
        markdown.append("\n");
    }

    private List<Map<String, String>> csvRows(String content) {
        List<List<String>> rows = parseCsv(content);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<String> headers = rows.get(0).stream().map(this::normalizeHeader).toList();
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.size() && j < rows.get(i).size(); j++) {
                row.put(headers.get(j), trimToNull(rows.get(i).get(j)));
            }
            result.add(row);
        }
        return result;
    }

    private List<List<String>> parseCsv(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < content.length() && content.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                row.add(cell.toString());
                cell.setLength(0);
            } else if ((c == '\n' || c == '\r') && !quoted) {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(cell.toString());
                cell.setLength(0);
                if (row.stream().anyMatch(StringUtils::hasText)) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        row.add(cell.toString());
        if (row.stream().anyMatch(StringUtils::hasText)) {
            rows.add(row);
        }
        return rows;
    }

    private String csv(List<List<String>> rows) {
        return rows.stream()
                .map(row -> row.stream().map(this::csvCell).reduce((left, right) -> left + "," + right).orElse(""))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private String csvCell(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private JsonNode readJson(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Import JSON is invalid.");
        }
    }

    private Iterable<JsonNode> array(JsonNode root, String field) {
        JsonNode node = root.path(field);
        return node.isArray() ? node : List.of();
    }

    private Optional<String> firstMarkdownHeading(String content) {
        return content.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private void collectPageWarnings(String content, ImportPlan plan) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (lower.contains("p.") || lower.contains("page") || content.contains("\u7b2c") || content.contains("\u9875")) {
            plan.warnings.add("Markdown page markers will be parsed by the deterministic note parser. Missing pages remain null.");
        }
    }

    private Integer page(JsonNode node, String field, ImportPlan plan) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || !StringUtils.hasText(value.asText())) {
            return null;
        }
        return parsePage(value.asText(), plan);
    }

    private Integer parsePage(String raw, ImportPlan plan) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            int page = Integer.parseInt(raw.trim());
            if (page < 1) {
                plan.pageNumberIssues.add("Ignored invalid non-positive page number: " + raw);
                return null;
            }
            return page;
        } catch (NumberFormatException exception) {
            plan.pageNumberIssues.add("Ignored malformed page number: " + raw);
            return null;
        }
    }

    private Long longOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || !StringUtils.hasText(value.asText())) {
            return null;
        }
        return value.canConvertToLong() ? value.asLong() : null;
    }

    private Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || !StringUtils.hasText(value.asText())) {
            return null;
        }
        return value.canConvertToInt() ? value.asInt() : null;
    }

    private int intOrDefault(JsonNode node, String field, int fallback) {
        Integer value = intOrNull(node, field);
        return value == null ? fallback : value;
    }

    private String requiredText(JsonNode node, String field, String fallback) {
        return Optional.ofNullable(textOrNull(node, field)).orElse(fallback);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : trimToNull(value.asText());
    }

    private List<String> strings(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        value.forEach(item -> {
            String text = trimToNull(item.asText());
            if (text != null && result.stream().noneMatch(existing -> existing.equalsIgnoreCase(text))) {
                result.add(text);
            }
        });
        return result;
    }

    private List<String> splitList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        String normalized = raw.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            try {
                JsonNode node = objectMapper.readTree(normalized);
                List<String> result = new ArrayList<>();
                node.forEach(item -> {
                    String text = trimToNull(item.asText());
                    if (text != null) {
                        result.add(text);
                    }
                });
                return result;
            } catch (Exception ignored) {
                // Fall through to delimiter parsing.
            }
        }
        return List.of(normalized.split("[;|]")).stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private KnowledgeObjectType knowledgeType(String raw, ImportPlan plan) {
        if (!StringUtils.hasText(raw)) {
            plan.unsupportedFields.add("Knowledge object without type was skipped.");
            return null;
        }
        try {
            return KnowledgeObjectType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            plan.unsupportedFields.add("Unsupported knowledge object type skipped: " + raw);
            return null;
        }
    }

    private SourceConfidence sourceConfidence(String raw, ImportPlan plan) {
        if (!StringUtils.hasText(raw)) {
            return SourceConfidence.LOW;
        }
        try {
            return SourceConfidence.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            plan.sourceReferenceIssues.add("Unsupported source confidence treated as LOW: " + raw);
            return SourceConfidence.LOW;
        }
    }

    private ActionPriority priority(String raw) {
        if (!StringUtils.hasText(raw)) {
            return ActionPriority.MEDIUM;
        }
        try {
            return ActionPriority.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ActionPriority.MEDIUM;
        }
    }

    private List<String> parseJsonList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (!node.isArray()) {
                return List.of();
            }
            List<String> result = new ArrayList<>();
            node.forEach(item -> result.add(item.asText()));
            return result;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<String> authorNames(Book book) {
        return book.getBookAuthors().stream()
                .map(BookAuthor::getAuthor)
                .map(author -> author.getName())
                .toList();
    }

    private List<String> tagNames(Book book) {
        return book.getBookTags().stream()
                .map(BookTag::getTag)
                .map(tag -> tag.getName())
                .toList();
    }

    private String normalizeImportType(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private String normalizeKey(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String normalizeHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("book title")) {
            return "bookTitle";
        }
        return trimmed.replace(" ", "");
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String stripExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        String value = fileName.trim();
        int index = value.lastIndexOf('.');
        return index > 0 ? value.substring(0, index) : value;
    }

    private String escapeMarkdown(String value) {
        return value == null ? "" : value.replace("#", "\\#");
    }

    private String pageSuffix(Integer pageStart, Integer pageEnd) {
        if (pageStart == null) {
            return "";
        }
        if (pageEnd != null) {
            return " p." + pageStart + "-" + pageEnd;
        }
        return " p." + pageStart;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int clamp(Integer value, int min, int max) {
        if (value == null) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private record ImportBookRecord(
            Long oldId,
            String title,
            String subtitle,
            String description,
            String isbn,
            String publisher,
            Integer publicationYear,
            String coverUrl,
            String category,
            List<String> authors,
            List<String> tags) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("BOOK", title, "book:" + title.toLowerCase(Locale.ROOT), false, "CREATE", null);
        }
    }

    private record ImportNoteRecord(Long oldId, Long oldBookId, String bookTitle, String title, String markdown, String summary) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("NOTE", title, "note:" + normalize(title, bookTitle), false, "CREATE", null);
        }
    }

    private record ImportCaptureRecord(Long oldId, Long oldBookId, String bookTitle, String rawText) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("CAPTURE", previewText(rawText), "capture:" + normalize(rawText, bookTitle), false, "CREATE", null);
        }
    }

    private record ImportQuoteRecord(
            Long oldId,
            Long oldBookId,
            String bookTitle,
            String text,
            String attribution,
            Long oldSourceReferenceId,
            Integer pageStart,
            Integer pageEnd,
            List<String> tags,
            List<String> concepts) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("QUOTE", previewText(text), "quote:" + normalize(text, bookTitle), false, "CREATE", null);
        }
    }

    private record ImportActionItemRecord(
            Long oldId,
            Long oldBookId,
            String bookTitle,
            String title,
            String description,
            Long oldSourceReferenceId,
            ActionPriority priority,
            Integer pageStart,
            Integer pageEnd) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("ACTION_ITEM", title, "action:" + normalize(title, bookTitle), false, "CREATE", null);
        }
    }

    private record ImportConceptRecord(
            Long oldId,
            Long oldBookId,
            String bookTitle,
            String name,
            String description,
            String ontologyLayer,
            Long oldSourceReferenceId,
            List<String> tags) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("CONCEPT", name, "concept:" + name.toLowerCase(Locale.ROOT), false, "CREATE", null);
        }
    }

    private record ImportKnowledgeObjectRecord(
            Long oldId,
            Long oldBookId,
            String bookTitle,
            KnowledgeObjectType type,
            String title,
            String description,
            String ontologyLayer,
            Long oldSourceReferenceId,
            List<String> tags) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("KNOWLEDGE_OBJECT", title, "knowledge:" + type + ":" + title.toLowerCase(Locale.ROOT), false, "CREATE", null);
        }
    }

    private record ImportSourceReferenceRecord(
            Long oldId,
            Long oldBookId,
            String bookTitle,
            String sourceType,
            Integer pageStart,
            Integer pageEnd,
            String locationLabel,
            String sourceText,
            SourceConfidence sourceConfidence) implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("SOURCE_REFERENCE", title(), stableKey(oldBookId), false, "CREATE_OR_REUSE", null);
        }

        private String title() {
            return sourceType + (pageStart == null ? "" : " p." + pageStart);
        }

        private String stableKey(Long bookId) {
            return "source:" + bookId + ":" + normalize(sourceType, sourceText, String.valueOf(pageStart), String.valueOf(pageEnd));
        }

        private String stableKey(SourceReference source) {
            return "source:" + source.getBook().getId() + ":" + normalize(
                    source.getSourceType(),
                    source.getSourceText(),
                    String.valueOf(source.getPageStart()),
                    String.valueOf(source.getPageEnd()));
        }

        private boolean duplicate(User user, SourceReferenceRepository repository) {
            Long bookId = oldBookId;
            if (bookId == null) {
                return false;
            }
            return repository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, user.getId()).stream()
                    .anyMatch(source -> stableKey(source).equals(stableKey(bookId)));
        }
    }

    private record ImportProjectRecord(Long oldId, String title, String description, String genre, String platform, String stage, Integer progressPercent)
            implements Previewable {
        @Override
        public ImportRecordPreview preview() {
            return new ImportRecordPreview("PROJECT", title, "project:" + title.toLowerCase(Locale.ROOT), false, "CREATE", null);
        }
    }

    private interface Previewable {
        ImportRecordPreview preview();
    }

    private static String previewText(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 80 ? value.substring(0, 77) + "..." : value;
    }

    private static String normalize(String... values) {
        if (values == null) {
            return "";
        }
        return Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .reduce((left, right) -> left + ":" + right)
                .orElse("")
                .toLowerCase(Locale.ROOT);
    }

    private static final class ImportPlan {
        private final String importType;
        private final List<ImportBookRecord> books = new ArrayList<>();
        private final List<ImportNoteRecord> notes = new ArrayList<>();
        private final List<ImportCaptureRecord> captures = new ArrayList<>();
        private final List<ImportQuoteRecord> quotes = new ArrayList<>();
        private final List<ImportActionItemRecord> actionItems = new ArrayList<>();
        private final List<ImportConceptRecord> concepts = new ArrayList<>();
        private final List<ImportKnowledgeObjectRecord> knowledgeObjects = new ArrayList<>();
        private final List<ImportSourceReferenceRecord> sourceReferences = new ArrayList<>();
        private final List<ImportProjectRecord> projects = new ArrayList<>();
        private final List<ImportRecordPreview> records = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> unsupportedFields = new ArrayList<>();
        private final List<String> sourceReferenceIssues = new ArrayList<>();
        private final List<String> pageNumberIssues = new ArrayList<>();

        private ImportPlan(String importType) {
            this.importType = importType;
        }

        private ImportPreviewResponse toPreview() {
            int duplicates = (int) records.stream().filter(ImportRecordPreview::duplicate).count();
            int create = (int) records.stream().filter(record -> !record.duplicate() && !"SOURCE_REFERENCE".equals(record.type())).count();
            return new ImportPreviewResponse(
                    importType,
                    records.size(),
                    create,
                    duplicates,
                    records,
                    warnings,
                    unsupportedFields,
                    sourceReferenceIssues,
                    pageNumberIssues);
        }
    }

    private static final class CommitStats {
        private int booksCreated;
        private int notesCreated;
        private int capturesCreated;
        private int quotesCreated;
        private int actionItemsCreated;
        private int conceptsCreated;
        private int knowledgeObjectsCreated;
        private int sourceReferencesCreated;
        private int projectsCreated;
        private int duplicatesSkipped;
        private final List<String> warnings;

        private CommitStats(List<String> warnings) {
            this.warnings = new ArrayList<>(warnings);
        }

        private ImportCommitResponse toResponse() {
            return new ImportCommitResponse(
                    booksCreated,
                    notesCreated,
                    capturesCreated,
                    quotesCreated,
                    actionItemsCreated,
                    conceptsCreated,
                    knowledgeObjectsCreated,
                    sourceReferencesCreated,
                    projectsCreated,
                    duplicatesSkipped,
                    warnings);
        }
    }
}
