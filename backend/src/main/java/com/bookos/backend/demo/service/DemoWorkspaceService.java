package com.bookos.backend.demo.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Author;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.BookAuthor;
import com.bookos.backend.book.entity.BookTag;
import com.bookos.backend.book.entity.Tag;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.AuthorRepository;
import com.bookos.backend.book.repository.BookAuthorRepository;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.BookTagRepository;
import com.bookos.backend.book.repository.TagRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.daily.entity.DailyDesignPrompt;
import com.bookos.backend.daily.entity.DailySentence;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.daily.repository.DailyHistoryRepository;
import com.bookos.backend.daily.repository.DailyReflectionRepository;
import com.bookos.backend.daily.repository.DailySentenceRepository;
import com.bookos.backend.demo.dto.DemoWorkspaceStatusResponse;
import com.bookos.backend.demo.entity.DemoRecord;
import com.bookos.backend.demo.repository.DemoRecordRepository;
import com.bookos.backend.forum.entity.ForumCategory;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumCategoryRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.project.entity.DesignDecision;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.PlaytestFinding;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.entity.ProjectProblem;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoWorkspaceService {

    public static final String TAG = "demo-workspace";
    private static final String SAFETY_NOTE =
            "Demo mode uses original BookOS sample content, stores unknown pages as null, and labels every created record as demo.";

    private final UserService userService;
    private final DemoRecordRepository demoRecordRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final AuthorRepository authorRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final TagRepository tagRepository;
    private final BookTagRepository bookTagRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectProblemRepository problemRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final DesignDecisionRepository decisionRepository;
    private final PlaytestFindingRepository findingRepository;
    private final ForumCategoryRepository categoryRepository;
    private final ForumThreadRepository threadRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final DailySentenceRepository dailySentenceRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final DailyReflectionRepository dailyReflectionRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public DemoWorkspaceStatusResponse status(String email) {
        User user = userService.getByEmailRequired(email);
        return status(user);
    }

    @Transactional
    public DemoWorkspaceStatusResponse start(String email) {
        User user = userService.getByEmailRequired(email);
        if (!demoRecordRepository.findByUserIdOrderByCreatedAtAsc(user.getId()).isEmpty()) {
            return status(user);
        }
        createDemoWorkspace(user);
        return status(user);
    }

    @Transactional
    public DemoWorkspaceStatusResponse reset(String email) {
        User user = userService.getByEmailRequired(email);
        deleteWorkspace(user);
        createDemoWorkspace(user);
        return status(user);
    }

    @Transactional
    public void delete(String email) {
        User user = userService.getByEmailRequired(email);
        deleteWorkspace(user);
    }

    @Transactional(readOnly = true)
    public Set<Long> demoIds(Long userId, String entityType) {
        return demoRecordRepository.findByUserIdAndEntityType(userId, entityType).stream()
                .map(DemoRecord::getEntityId)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<Long> demoIds(Long userId, Collection<String> entityTypes) {
        return demoRecordRepository.findByUserIdAndEntityTypeIn(userId, entityTypes).stream()
                .map(DemoRecord::getEntityId)
                .collect(Collectors.toSet());
    }

    private DemoWorkspaceStatusResponse status(User user) {
        List<DemoRecord> records = demoRecordRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        Map<String, Long> counts = records.stream()
                .collect(Collectors.groupingBy(DemoRecord::getEntityType, LinkedHashMap::new, Collectors.counting()));
        Instant lastResetAt = records.stream()
                .map(DemoRecord::getCreatedAt)
                .min(Instant::compareTo)
                .orElse(null);
        List<String> includedRecordTypes = records.stream()
                .map(DemoRecord::getEntityType)
                .distinct()
                .sorted()
                .toList();
        return new DemoWorkspaceStatusResponse(
                !records.isEmpty(),
                lastResetAt,
                firstId(records, "BOOK"),
                firstId(records, "GAME_PROJECT"),
                firstId(records, "QUOTE"),
                firstId(records, "ACTION_ITEM"),
                firstId(records, "FORUM_THREAD"),
                records.stream()
                        .filter(record -> "CONCEPT".equals(record.getEntityType()))
                        .map(DemoRecord::getEntityId)
                        .toList(),
                counts,
                includedRecordTypes,
                true,
                "Demo Workspace",
                SAFETY_NOTE);
    }

    private void createDemoWorkspace(User user) {
        Book book = createBook(user);
        SourceReference bookSource = createSource(user, book, null, "DEMO_BOOK", "Demo workspace - page unknown",
                "Original BookOS demo notebook. This is not book text; page numbers are intentionally unknown.");
        List<RawCapture> captures = createCaptures(user, book);
        for (RawCapture capture : captures) {
            createSource(user, book, capture, "DEMO_CAPTURE", "Demo capture - page unknown", capture.getRawText());
        }
        Quote quote = createQuote(user, book, captures.get(1), sourceForCapture(user, captures.get(1)));
        ActionItem actionItem = createActionItem(user, book, captures.get(2), sourceForCapture(user, captures.get(2)));
        List<Concept> concepts = createConcepts(user, book, bookSource);
        List<KnowledgeObject> knowledgeObjects = createKnowledgeObjects(user, book, concepts, bookSource);
        GameProject project = createProject(user);
        ProjectProblem problem = createProblem(project, bookSource);
        ProjectApplication application = createApplication(project, quote, sourceForId(user, quote.getSourceReferenceId()));
        createDecision(project, bookSource);
        createFinding(project, bookSource);
        ForumThread thread = createForumThread(user, book, concepts.get(0), project, bookSource);
        createLinks(user, book, bookSource, quote, actionItem, concepts, knowledgeObjects, project, problem, application, thread);
    }

    private Book createBook(User user) {
        Book book = new Book();
        book.setOwner(user);
        book.setTitle("Demo Game Design Notebook");
        book.setSubtitle("Original sample workspace for learning BookOS");
        book.setDescription("Demo-labeled original sample material for practicing capture, conversion, source reference, and project workflows.");
        book.setCategory("Demo Workspace");
        book.setVisibility(Visibility.PRIVATE);
        Book saved = bookRepository.save(book);

        Author author = authorRepository.findByNameIgnoreCase("BookOS Demo").orElseGet(() -> {
            Author created = new Author();
            created.setName("BookOS Demo");
            created.setSlug("bookos-demo");
            return authorRepository.save(created);
        });
        BookAuthor link = new BookAuthor();
        link.setBook(saved);
        link.setAuthor(author);
        link.setDisplayOrder(0);
        bookAuthorRepository.save(link);

        Tag tag = tagRepository.findBySlugIgnoreCase(TAG).orElseGet(() -> {
            Tag created = new Tag();
            created.setName("Demo Workspace");
            created.setSlug(TAG);
            return tagRepository.save(created);
        });
        BookTag bookTag = new BookTag();
        bookTag.setBook(saved);
        bookTag.setTag(tag);
        bookTagRepository.save(bookTag);

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(saved);
        userBook.setReadingStatus(ReadingStatus.CURRENTLY_READING);
        userBook.setReadingFormat(ReadingFormat.OTHER);
        userBook.setOwnershipStatus(OwnershipStatus.SAMPLE);
        userBook.setProgressPercent(20);
        userBook.setRating(5);
        record("BOOK", saved.getId(), user);
        record("USER_BOOK", userBookRepository.save(userBook).getId(), user);
        return saved;
    }

    private List<RawCapture> createCaptures(User user, Book book) {
        return List.of(
                createCapture(user, book, "💡 This tutorial room could teach the core loop through one clear repeatable interaction. #demo [[Core Loop]]", NoteBlockType.INSPIRATION, List.of("demo"), List.of("Core Loop")),
                createCapture(user, book, "💬 \"A good first puzzle teaches one action before it tests mastery.\" #demo [[Feedback Loop]]", NoteBlockType.QUOTE, List.of("demo"), List.of("Feedback Loop")),
                createCapture(user, book, "✅ Test whether the player notices the glowing door after the first pickup. #demo [[Game Feel]]", NoteBlockType.ACTION_ITEM, List.of("demo"), List.of("Game Feel")),
                createCapture(user, book, "🧩 The choice should feel meaningful before it becomes difficult. #demo [[Meaningful Choice]]", NoteBlockType.RELATED_CONCEPT, List.of("demo"), List.of("Meaningful Choice")));
    }

    private RawCapture createCapture(User user, Book book, String rawText, NoteBlockType type, List<String> tags, List<String> concepts) {
        RawCapture capture = new RawCapture();
        capture.setUser(user);
        capture.setBook(book);
        capture.setRawText(rawText);
        capture.setCleanText(rawText.replaceFirst("^\\p{So}\\s*", "").trim());
        capture.setParsedType(type);
        capture.setPageStart(null);
        capture.setPageEnd(null);
        capture.setTagsJson(writeList(tags));
        capture.setConceptsJson(writeList(concepts));
        capture.setParserWarningsJson(writeList(List.of("Demo sample: no page marker provided; page remains unknown.")));
        capture.setStatus(CaptureStatus.INBOX);
        RawCapture saved = rawCaptureRepository.save(capture);
        record("RAW_CAPTURE", saved.getId(), user);
        return saved;
    }

    private SourceReference createSource(User user, Book book, RawCapture capture, String sourceType, String locationLabel, String sourceText) {
        SourceReference source = new SourceReference();
        source.setUser(user);
        source.setBook(book);
        source.setRawCaptureId(capture == null ? null : capture.getId());
        source.setSourceType(sourceType);
        source.setPageStart(null);
        source.setPageEnd(null);
        source.setLocationLabel(locationLabel);
        source.setSourceText(sourceText);
        source.setSourceConfidence(SourceConfidence.LOW);
        SourceReference saved = sourceReferenceRepository.save(source);
        record("SOURCE_REFERENCE", saved.getId(), user);
        return saved;
    }

    private Quote createQuote(User user, Book book, RawCapture capture, SourceReference source) {
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setBook(book);
        quote.setRawCaptureId(capture.getId());
        quote.setSourceReferenceId(source.getId());
        quote.setText("A good first puzzle teaches one action before it tests mastery.");
        quote.setAttribution("BookOS Demo original sample");
        quote.setPageStart(null);
        quote.setPageEnd(null);
        quote.setTagsJson(writeList(List.of("demo", TAG)));
        quote.setConceptsJson(writeList(List.of("Feedback Loop")));
        quote.setVisibility(Visibility.PRIVATE);
        Quote saved = quoteRepository.save(quote);
        record("QUOTE", saved.getId(), user);
        return saved;
    }

    private ActionItem createActionItem(User user, Book book, RawCapture capture, SourceReference source) {
        ActionItem item = new ActionItem();
        item.setUser(user);
        item.setBook(book);
        item.setRawCaptureId(capture.getId());
        item.setSourceReferenceId(source.getId());
        item.setTitle("[Demo] Test glowing-door readability");
        item.setDescription("Original demo action: check whether the player notices the next interaction after the first pickup.");
        item.setPriority(ActionPriority.MEDIUM);
        item.setPageStart(null);
        item.setPageEnd(null);
        item.setVisibility(Visibility.PRIVATE);
        ActionItem saved = actionItemRepository.save(item);
        record("ACTION_ITEM", saved.getId(), user);
        return saved;
    }

    private List<Concept> createConcepts(User user, Book book, SourceReference source) {
        return List.of(
                createConcept(user, book, source, "Core Loop", "The smallest repeatable action pattern the player learns and repeats in the demo."),
                createConcept(user, book, source, "Feedback Loop", "The response cycle that tells the player what happened and what to try next."),
                createConcept(user, book, source, "Game Feel", "The perceived responsiveness, timing, and clarity of interaction feedback."),
                createConcept(user, book, source, "Meaningful Choice", "A decision whose consequences are readable enough for the player to care."));
    }

    private Concept createConcept(User user, Book book, SourceReference source, String name, String description) {
        Concept concept = new Concept();
        concept.setUser(user);
        concept.setName(name);
        concept.setSlug("demo-" + SlugUtils.slugify(name) + "-" + user.getId());
        concept.setDescription(description);
        concept.setOntologyLayer("Demo Workspace");
        concept.setSourceConfidence(SourceConfidence.LOW);
        concept.setCreatedBy("DEMO");
        concept.setTagsJson(writeList(List.of("demo", TAG)));
        concept.setVisibility(Visibility.PRIVATE);
        concept.setFirstBook(book);
        concept.setFirstSourceReference(source);
        concept.setMentionCount(1);
        Concept saved = conceptRepository.save(concept);
        record("CONCEPT", saved.getId(), user);
        return saved;
    }

    private List<KnowledgeObject> createKnowledgeObjects(User user, Book book, List<Concept> concepts, SourceReference source) {
        return List.of(
                createKnowledgeObject(user, book, concepts.get(1), source, KnowledgeObjectType.DESIGN_LENS, "Demo Lens: Clear Feedback",
                        "Ask whether the player can tell what changed, why it changed, and what to try next."),
                createKnowledgeObject(user, book, concepts.get(0), source, KnowledgeObjectType.PROTOTYPE_TASK, "Demo Prototype Task: One Repeatable Loop",
                        "Build one small interaction loop with a clear input, response, reward, and next prompt."));
    }

    private KnowledgeObject createKnowledgeObject(
            User user,
            Book book,
            Concept concept,
            SourceReference source,
            KnowledgeObjectType type,
            String title,
            String description) {
        KnowledgeObject object = new KnowledgeObject();
        object.setUser(user);
        object.setBook(book);
        object.setConcept(concept);
        object.setType(type);
        object.setTitle(title);
        object.setSlug("demo-" + SlugUtils.slugify(title) + "-" + user.getId());
        object.setDescription(description);
        object.setOntologyLayer("Demo Workspace");
        object.setSourceConfidence(SourceConfidence.LOW);
        object.setCreatedBy("DEMO");
        object.setVisibility(Visibility.PRIVATE);
        object.setSourceReferenceId(source.getId());
        object.setTagsJson(writeList(List.of("demo", TAG)));
        KnowledgeObject saved = knowledgeObjectRepository.save(object);
        record("KNOWLEDGE_OBJECT", saved.getId(), user);
        return saved;
    }

    private GameProject createProject(User user) {
        GameProject project = new GameProject();
        project.setOwner(user);
        project.setTitle("Demo Puzzle Adventure");
        project.setSlug(uniqueProjectSlug(user, "demo-puzzle-adventure"));
        project.setDescription("A clearly labeled demo project for practicing source-backed game design workflows.");
        project.setGenre("Puzzle Adventure");
        project.setPlatform("Desktop");
        project.setStage("PROTOTYPE");
        project.setVisibility(Visibility.PRIVATE);
        project.setProgressPercent(15);
        GameProject saved = projectRepository.save(project);
        record("GAME_PROJECT", saved.getId(), user);
        return saved;
    }

    private ProjectProblem createProblem(GameProject project, SourceReference source) {
        ProjectProblem problem = new ProjectProblem();
        problem.setProject(project);
        problem.setTitle("[Demo] First puzzle readability is uncertain");
        problem.setDescription("The player may not understand which interaction should happen after the first pickup.");
        problem.setStatus("OPEN");
        problem.setPriority("MEDIUM");
        problem.setRelatedSourceReference(source);
        ProjectProblem saved = problemRepository.save(problem);
        record("PROJECT_PROBLEM", saved.getId(), project.getOwner());
        return saved;
    }

    private ProjectApplication createApplication(GameProject project, Quote quote, SourceReference source) {
        ProjectApplication application = new ProjectApplication();
        application.setProject(project);
        application.setSourceEntityType("QUOTE");
        application.setSourceEntityId(quote.getId());
        application.setSourceReference(source);
        application.setApplicationType("PROJECT_APPLICATION");
        application.setTitle("[Demo] Apply first-puzzle teaching quote");
        application.setDescription("Use the original demo quote as a reminder to teach one action before asking for mastery.");
        application.setStatus("OPEN");
        ProjectApplication saved = applicationRepository.save(application);
        record("PROJECT_APPLICATION", saved.getId(), project.getOwner());
        return saved;
    }

    private void createDecision(GameProject project, SourceReference source) {
        DesignDecision decision = new DesignDecision();
        decision.setProject(project);
        decision.setTitle("[Demo] Gate the second mechanic until feedback is clear");
        decision.setDecision("Introduce the first interaction with a visible response before adding a second mechanic.");
        decision.setRationale("The player should understand the loop before the puzzle asks for mastery.");
        decision.setTradeoffs("The opening may feel slower, but the first decision becomes easier to read.");
        decision.setSourceReference(source);
        decision.setStatus("PROPOSED");
        DesignDecision saved = decisionRepository.save(decision);
        record("DESIGN_DECISION", saved.getId(), project.getOwner());
    }

    private void createFinding(GameProject project, SourceReference source) {
        PlaytestFinding finding = new PlaytestFinding();
        finding.setProject(project);
        finding.setTitle("[Demo] Players hesitate before the highlighted door");
        finding.setObservation("Demo observation: the next interaction is visible but not yet legible enough.");
        finding.setSeverity("MEDIUM");
        finding.setRecommendation("Add stronger feedback after the pickup and retest the first thirty seconds.");
        finding.setSourceReference(source);
        finding.setStatus("OPEN");
        PlaytestFinding saved = findingRepository.save(finding);
        record("PLAYTEST_FINDING", saved.getId(), project.getOwner());
    }

    private ForumThread createForumThread(User user, Book book, Concept concept, GameProject project, SourceReference source) {
        ForumThread thread = new ForumThread();
        thread.setCategory(resolveDemoCategory());
        thread.setAuthor(user);
        thread.setTitle("[Demo] Discussion: how should the first puzzle teach feedback?");
        thread.setBodyMarkdown("""
                This is a clearly labeled demo discussion using original BookOS sample material.

                Prompt: how would you teach the first interaction before testing mastery?

                Safety note: no copyrighted passages or real page numbers are used.
                """.trim());
        thread.setRelatedEntityType("GAME_PROJECT");
        thread.setRelatedEntityId(project.getId());
        thread.setRelatedBook(book);
        thread.setRelatedConcept(concept);
        thread.setRelatedProject(project);
        thread.setSourceReferenceId(source.getId());
        thread.setStatus(ForumThreadStatus.OPEN);
        thread.setVisibility(Visibility.PRIVATE);
        ForumThread saved = threadRepository.save(thread);
        record("FORUM_THREAD", saved.getId(), user);
        return saved;
    }

    private void createLinks(
            User user,
            Book book,
            SourceReference source,
            Quote quote,
            ActionItem actionItem,
            List<Concept> concepts,
            List<KnowledgeObject> knowledgeObjects,
            GameProject project,
            ProjectProblem problem,
            ProjectApplication application,
            ForumThread thread) {
        for (Concept concept : concepts) {
            createLink(user, "BOOK", book.getId(), "CONCEPT", concept.getId(), "MENTIONS", source.getId());
        }
        createLink(user, "QUOTE", quote.getId(), "CONCEPT", concepts.get(1).getId(), "MENTIONS", source.getId());
        createLink(user, "ACTION_ITEM", actionItem.getId(), "CONCEPT", concepts.get(2).getId(), "RELATED_TO", source.getId());
        createLink(user, "KNOWLEDGE_OBJECT", knowledgeObjects.get(0).getId(), "CONCEPT", concepts.get(1).getId(), "RELATED_TO", source.getId());
        createLink(user, "PROJECT_APPLICATION", application.getId(), "GAME_PROJECT", project.getId(), "APPLIES_TO", source.getId());
        createLink(user, "PROJECT_PROBLEM", problem.getId(), "GAME_PROJECT", project.getId(), "RELATED_TO", source.getId());
        createLink(user, "FORUM_THREAD", thread.getId(), "GAME_PROJECT", project.getId(), "DISCUSSES", source.getId());
    }

    private void createLink(User user, String sourceType, Long sourceId, String targetType, Long targetId, String relationType, Long sourceReferenceId) {
        EntityLink link = new EntityLink();
        link.setUser(user);
        link.setSourceType(sourceType);
        link.setSourceId(sourceId);
        link.setTargetType(targetType);
        link.setTargetId(targetId);
        link.setRelationType(relationType);
        link.setSourceReferenceId(sourceReferenceId);
        link.setNote("Demo workspace relationship; user-owned and removable.");
        link.setCreatedBy("DEMO");
        EntityLink saved = entityLinkRepository.save(link);
        record("ENTITY_LINK", saved.getId(), user);
    }

    private void deleteWorkspace(User user) {
        List<DemoRecord> records = demoRecordRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        if (records.isEmpty()) {
            return;
        }
        deleteDemoDailyRecords(user, records);
        deleteByType(records, "ENTITY_LINK", entityLinkRepository);
        deleteByType(records, "FORUM_THREAD", threadRepository);
        deleteByType(records, "PLAYTEST_FINDING", findingRepository);
        deleteByType(records, "DESIGN_DECISION", decisionRepository);
        deleteByType(records, "PROJECT_APPLICATION", applicationRepository);
        deleteByType(records, "PROJECT_PROBLEM", problemRepository);
        deleteByType(records, "GAME_PROJECT", projectRepository);
        deleteByType(records, "ACTION_ITEM", actionItemRepository);
        deleteByType(records, "QUOTE", quoteRepository);
        deleteByType(records, "KNOWLEDGE_OBJECT", knowledgeObjectRepository);
        deleteByType(records, "CONCEPT", conceptRepository);
        deleteByType(records, "RAW_CAPTURE", rawCaptureRepository);
        deleteByType(records, "SOURCE_REFERENCE", sourceReferenceRepository);
        deleteByType(records, "USER_BOOK", userBookRepository);
        records.stream()
                .filter(record -> "BOOK".equals(record.getEntityType()))
                .map(DemoRecord::getEntityId)
                .forEach(bookId -> {
                    bookAuthorRepository.deleteAllByBookId(bookId);
                    bookTagRepository.deleteAllByBookId(bookId);
                });
        deleteByType(records, "BOOK", bookRepository);
        demoRecordRepository.deleteAll(records);
    }

    private <T> void deleteByType(List<DemoRecord> records, String entityType, org.springframework.data.jpa.repository.JpaRepository<T, Long> repository) {
        List<Long> ids = records.stream()
                .filter(record -> entityType.equals(record.getEntityType()))
                .map(DemoRecord::getEntityId)
                .toList();
        if (!ids.isEmpty()) {
            repository.deleteAllByIdInBatch(ids);
        }
    }

    private void deleteDemoDailyRecords(User user, List<DemoRecord> records) {
        Set<Long> bookIds = idsFor(records, "BOOK");
        Set<Long> knowledgeObjectIds = idsFor(records, "KNOWLEDGE_OBJECT");
        Set<Long> sourceReferenceIds = idsFor(records, "SOURCE_REFERENCE");
        Set<Long> quoteIds = idsFor(records, "QUOTE");
        Set<Long> rawCaptureIds = idsFor(records, "RAW_CAPTURE");

        List<Long> promptIds = dailyDesignPromptRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(prompt -> isDemoPrompt(prompt, bookIds, knowledgeObjectIds, sourceReferenceIds))
                .map(DailyDesignPrompt::getId)
                .toList();
        List<Long> sentenceIds = dailySentenceRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(sentence -> isDemoSentence(sentence, bookIds, sourceReferenceIds, quoteIds, rawCaptureIds))
                .map(DailySentence::getId)
                .toList();

        if (!sentenceIds.isEmpty()) {
            dailyReflectionRepository.deleteByUserIdAndDailySentenceIds(user.getId(), sentenceIds);
            dailyHistoryRepository.deleteByUserIdAndDailySentenceIds(user.getId(), sentenceIds);
            dailySentenceRepository.deleteAllByIdInBatch(sentenceIds);
        }
        if (!promptIds.isEmpty()) {
            dailyReflectionRepository.deleteByUserIdAndDailyDesignPromptIds(user.getId(), promptIds);
            dailyHistoryRepository.deleteByUserIdAndDailyDesignPromptIds(user.getId(), promptIds);
            dailyDesignPromptRepository.deleteAllByIdInBatch(promptIds);
        }
    }

    private boolean isDemoPrompt(
            DailyDesignPrompt prompt,
            Set<Long> bookIds,
            Set<Long> knowledgeObjectIds,
            Set<Long> sourceReferenceIds) {
        return (prompt.getBook() != null && bookIds.contains(prompt.getBook().getId()))
                || (prompt.getKnowledgeObject() != null && knowledgeObjectIds.contains(prompt.getKnowledgeObject().getId()))
                || (prompt.getSourceReferenceId() != null && sourceReferenceIds.contains(prompt.getSourceReferenceId()))
                || ("KNOWLEDGE_OBJECT".equals(prompt.getSourceType()) && knowledgeObjectIds.contains(prompt.getSourceId()));
    }

    private boolean isDemoSentence(
            DailySentence sentence,
            Set<Long> bookIds,
            Set<Long> sourceReferenceIds,
            Set<Long> quoteIds,
            Set<Long> rawCaptureIds) {
        return (sentence.getBook() != null && bookIds.contains(sentence.getBook().getId()))
                || (sentence.getSourceReferenceId() != null && sourceReferenceIds.contains(sentence.getSourceReferenceId()))
                || ("QUOTE".equals(sentence.getSourceType()) && quoteIds.contains(sentence.getSourceId()))
                || ("RAW_CAPTURE".equals(sentence.getSourceType()) && rawCaptureIds.contains(sentence.getSourceId()));
    }

    private Set<Long> idsFor(List<DemoRecord> records, String entityType) {
        return records.stream()
                .filter(record -> entityType.equals(record.getEntityType()))
                .map(DemoRecord::getEntityId)
                .collect(Collectors.toSet());
    }

    private void record(String entityType, Long entityId, User user) {
        if (entityId == null || demoRecordRepository.findByUserIdAndEntityTypeAndEntityId(user.getId(), entityType, entityId).isPresent()) {
            return;
        }
        DemoRecord record = new DemoRecord();
        record.setUser(user);
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        demoRecordRepository.save(record);
    }

    private SourceReference sourceForCapture(User user, RawCapture capture) {
        return sourceReferenceRepository.findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(capture.getId(), user.getId()).stream()
                .findFirst()
                .orElseThrow();
    }

    private SourceReference sourceForId(User user, Long id) {
        return sourceReferenceRepository.findByIdAndUserId(id, user.getId()).orElseThrow();
    }

    private ForumCategory resolveDemoCategory() {
        return categoryRepository.findBySlug("project-critiques")
                .or(() -> categoryRepository.findBySlug("general"))
                .orElseGet(() -> {
                    ForumCategory category = new ForumCategory();
                    category.setName("General");
                    category.setSlug("general");
                    category.setDescription("General BookOS discussion.");
                    category.setSortOrder(999);
                    return categoryRepository.save(category);
                });
    }

    private String uniqueProjectSlug(User user, String base) {
        String slug = base;
        int suffix = 2;
        while (projectRepository.existsByOwnerIdAndSlug(user.getId(), slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private Long firstId(List<DemoRecord> records, String entityType) {
        return records.stream()
                .filter(record -> entityType.equals(record.getEntityType()))
                .map(DemoRecord::getEntityId)
                .findFirst()
                .orElse(null);
    }

    private String writeList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize demo metadata.", exception);
        }
    }
}
