package com.bookos.backend.learning.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.daily.repository.DailyReflectionRepository;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.learning.dto.AnalyticsCountResponse;
import com.bookos.backend.learning.dto.BookAnalyticsResponse;
import com.bookos.backend.learning.dto.KnowledgeAnalyticsResponse;
import com.bookos.backend.learning.dto.KnowledgeMasteryRequest;
import com.bookos.backend.learning.dto.KnowledgeMasteryResponse;
import com.bookos.backend.learning.dto.ReadingAnalyticsResponse;
import com.bookos.backend.learning.dto.ReadingSessionFinishRequest;
import com.bookos.backend.learning.dto.ReadingSessionResponse;
import com.bookos.backend.learning.dto.ReadingSessionStartRequest;
import com.bookos.backend.learning.dto.ReviewGenerateRequest;
import com.bookos.backend.learning.dto.ReviewItemRequest;
import com.bookos.backend.learning.dto.ReviewItemResponse;
import com.bookos.backend.learning.dto.ReviewItemUpdateRequest;
import com.bookos.backend.learning.dto.ReviewSessionRequest;
import com.bookos.backend.learning.dto.ReviewSessionResponse;
import com.bookos.backend.learning.entity.KnowledgeMastery;
import com.bookos.backend.learning.entity.ReadingSession;
import com.bookos.backend.learning.entity.ReviewItem;
import com.bookos.backend.learning.entity.ReviewSession;
import com.bookos.backend.learning.repository.KnowledgeMasteryRepository;
import com.bookos.backend.learning.repository.ReadingSessionRepository;
import com.bookos.backend.learning.repository.ReviewItemRepository;
import com.bookos.backend.learning.repository.ReviewSessionRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.PlaytestFinding;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.entity.ProjectLensReview;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final UserService userService;
    private final BookService bookService;
    private final SourceReferenceService sourceReferenceService;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final KnowledgeMasteryRepository masteryRepository;
    private final UserBookRepository userBookRepository;
    private final BookNoteRepository bookNoteRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final DailyReflectionRepository dailyReflectionRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final PlaytestFindingRepository playtestFindingRepository;
    private final ProjectLensReviewRepository projectLensReviewRepository;

    @Transactional(readOnly = true)
    public List<ReadingSessionResponse> listReadingSessions(String email) {
        User user = userService.getByEmailRequired(email);
        return readingSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .map(this::toReadingSessionResponse)
                .toList();
    }

    @Transactional
    public ReadingSessionResponse startReadingSession(String email, ReadingSessionStartRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, request.bookId());
        ReadingSession session = new ReadingSession();
        session.setUser(user);
        session.setBook(book);
        session.setStartedAt(Instant.now());
        session.setStartPage(nonNegative(request.startPage()));
        session.setReflection(trimToNull(request.reflection()));
        return toReadingSessionResponse(readingSessionRepository.save(session));
    }

    @Transactional
    public ReadingSessionResponse finishReadingSession(String email, Long id, ReadingSessionFinishRequest request) {
        User user = userService.getByEmailRequired(email);
        ReadingSession session = readingSessionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Reading session not found."));
        session.setEndedAt(Instant.now());
        session.setEndPage(nonNegative(request.endPage()));
        session.setMinutesRead(resolveMinutes(session, request.minutesRead()));
        session.setNotesCount(nonNegativeOrDefault(request.notesCount(), session.getNotesCount()));
        session.setCapturesCount(nonNegativeOrDefault(request.capturesCount(), session.getCapturesCount()));
        if (StringUtils.hasText(request.reflection())) {
            session.setReflection(request.reflection().trim());
        }
        return toReadingSessionResponse(readingSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public List<ReadingSessionResponse> listBookReadingSessions(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        bookService.getAccessibleBookEntity(email, bookId);
        return readingSessionRepository.findByUserIdAndBookIdOrderByStartedAtDesc(user.getId(), bookId).stream()
                .map(this::toReadingSessionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewSessionResponse> listReviewSessions(String email) {
        User user = userService.getByEmailRequired(email);
        return reviewSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .map(session -> toReviewSessionResponse(user.getId(), session, false))
                .toList();
    }

    @Transactional
    public ReviewSessionResponse createReviewSession(String email, ReviewSessionRequest request) {
        User user = userService.getByEmailRequired(email);
        ReviewSession session = new ReviewSession();
        session.setUser(user);
        session.setTitle(request.title().trim());
        session.setStartedAt(Instant.now());
        session.setMode(defaultString(request.mode(), "SOURCE_REVIEW"));
        session.setScopeType(defaultString(request.scopeType(), "GENERAL"));
        session.setScopeId(request.scopeId());
        session.setSummary(trimToNull(request.summary()));
        return toReviewSessionResponse(user.getId(), reviewSessionRepository.save(session), true);
    }

    @Transactional(readOnly = true)
    public ReviewSessionResponse getReviewSession(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ReviewSession session = reviewSessionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Review session not found."));
        return toReviewSessionResponse(user.getId(), session, true);
    }

    @Transactional
    public ReviewItemResponse addReviewItem(String email, Long sessionId, ReviewItemRequest request) {
        User user = userService.getByEmailRequired(email);
        ReviewSession session = reviewSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Review session not found."));
        assertTargetReadable(user, request.targetType(), request.targetId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : ownedSource(user, request.sourceReferenceId());
        ReviewItem item = new ReviewItem();
        item.setReviewSession(session);
        item.setTargetType(normalizeType(request.targetType()));
        item.setTargetId(request.targetId());
        item.setSourceReference(sourceReference);
        item.setPrompt(request.prompt().trim());
        return toReviewItemResponse(reviewItemRepository.save(item));
    }

    @Transactional
    public ReviewItemResponse updateReviewItem(String email, Long id, ReviewItemUpdateRequest request) {
        User user = userService.getByEmailRequired(email);
        ReviewItem item = reviewItemRepository.findByIdAndReviewSessionUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Review item not found."));
        item.setUserResponse(trimToNull(request.userResponse()));
        if (StringUtils.hasText(request.status())) {
            item.setStatus(normalizeType(request.status()));
        }
        if (request.confidenceScore() != null) {
            item.setConfidenceScore(request.confidenceScore());
        }
        ReviewItem saved = reviewItemRepository.save(item);
        if ("COMPLETED".equals(saved.getStatus()) || saved.getConfidenceScore() != null) {
            upsertMastery(user, saved.getTargetType(), saved.getTargetId(), saved.getConfidenceScore(), null, saved.getSourceReference());
        }
        completeSessionIfDone(saved.getReviewSession(), user.getId());
        return toReviewItemResponse(saved);
    }

    @Transactional
    public ReviewSessionResponse generateReviewFromBook(String email, ReviewGenerateRequest request) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, request.id());
        ReviewSession session = createGeneratedSession(user, "BOOK", book.getId(), request.title(), request.mode(), "Review " + book.getTitle());
        int limit = generationLimit(request.limit());
        List<ReviewItem> items = new ArrayList<>();
        quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), book.getId()).stream()
                .limit(limit)
                .forEach(quote -> items.add(reviewItem(session, "QUOTE", quote.getId(), sourceById(user, quote.getSourceReferenceId()), "Recall and apply this quote: " + excerpt(quote.getText()))));
        actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), book.getId()).stream()
                .limit(limit)
                .forEach(item -> items.add(reviewItem(session, "ACTION_ITEM", item.getId(), sourceById(user, item.getSourceReferenceId()), "What action should follow from: " + item.getTitle())));
        conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), book.getId()).stream()
                .limit(limit)
                .forEach(concept -> items.add(reviewItem(session, "CONCEPT", concept.getId(), concept.getFirstSourceReference(), "Explain this concept from the book: " + concept.getName())));
        reviewItemRepository.saveAll(items.stream().limit(limit).toList());
        return toReviewSessionResponse(user.getId(), session, true);
    }

    @Transactional
    public ReviewSessionResponse generateReviewFromConcept(String email, ReviewGenerateRequest request) {
        User user = userService.getByEmailRequired(email);
        Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(request.id(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Concept not found."));
        ReviewSession session = createGeneratedSession(user, "CONCEPT", concept.getId(), request.title(), request.mode(), "Review " + concept.getName());
        reviewItemRepository.save(reviewItem(session, "CONCEPT", concept.getId(), concept.getFirstSourceReference(), "Explain and apply this concept: " + concept.getName()));
        return toReviewSessionResponse(user.getId(), session, true);
    }

    @Transactional
    public ReviewSessionResponse generateReviewFromProject(String email, ReviewGenerateRequest request) {
        User user = userService.getByEmailRequired(email);
        GameProject project = projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(request.id(), user.getId())
                .orElseThrow(() -> new NoSuchElementException("Project not found."));
        ReviewSession session = createGeneratedSession(user, "PROJECT", project.getId(), request.title(), request.mode(), "Review project " + project.getTitle());
        int limit = generationLimit(request.limit());
        List<ReviewItem> items = new ArrayList<>();
        projectApplicationRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), user.getId()).stream()
                .limit(limit)
                .forEach(application -> items.add(reviewItem(session, "PROJECT_APPLICATION", application.getId(), application.getSourceReference(), "What design action follows from: " + application.getTitle())));
        projectLensReviewRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), user.getId()).stream()
                .limit(limit)
                .forEach(review -> items.add(reviewItem(session, "PROJECT_LENS_REVIEW", review.getId(), review.getSourceReference(), "Re-evaluate this lens answer: " + review.getQuestion())));
        playtestFindingRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), user.getId()).stream()
                .limit(limit)
                .forEach(finding -> items.add(reviewItem(session, "PLAYTEST_FINDING", finding.getId(), finding.getSourceReference(), "What iteration follows from this finding: " + finding.getTitle())));
        reviewItemRepository.saveAll(items.stream().limit(limit).toList());
        return toReviewSessionResponse(user.getId(), session, true);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeMasteryResponse> listMastery(String email) {
        User user = userService.getByEmailRequired(email);
        return masteryRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(this::toMasteryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeMasteryResponse getMasteryTarget(String email, String targetType, Long targetId) {
        User user = userService.getByEmailRequired(email);
        return masteryRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), normalizeType(targetType), targetId)
                .map(this::toMasteryResponse)
                .orElseThrow(() -> new NoSuchElementException("Mastery target not found."));
    }

    @Transactional
    public KnowledgeMasteryResponse updateMasteryTarget(String email, KnowledgeMasteryRequest request) {
        User user = userService.getByEmailRequired(email);
        assertTargetReadable(user, request.targetType(), request.targetId());
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : ownedSource(user, request.sourceReferenceId());
        KnowledgeMastery mastery = upsertMastery(user, request.targetType(), request.targetId(), request.familiarityScore(), request.usefulnessScore(), sourceReference);
        mastery.setNextReviewAt(request.nextReviewAt());
        return toMasteryResponse(masteryRepository.save(mastery));
    }

    @Transactional
    public void recordDailyReview(User user, String targetType, Long targetId, Long sourceReferenceId) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            return;
        }
        SourceReference sourceReference = sourceById(user, sourceReferenceId);
        upsertMastery(user, targetType, targetId, 1, 1, sourceReference);
    }

    @Transactional(readOnly = true)
    public ReadingAnalyticsResponse readingAnalytics(String email) {
        User user = userService.getByEmailRequired(email);
        List<UserBook> library = userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        List<ActionItem> actionItems = actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId());
        List<ReadingSession> readingSessions = readingSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        long totalMinutes = readingSessions.stream().map(ReadingSession::getMinutesRead).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
        return new ReadingAnalyticsResponse(
                library.size(),
                library.stream().filter(item -> item.getReadingStatus() == ReadingStatus.CURRENTLY_READING).count(),
                library.stream().filter(item -> item.getReadingStatus() == ReadingStatus.COMPLETED).count(),
                bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).size(),
                rawCaptureRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size(),
                quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).size(),
                actionItems.stream().filter(item -> item.getCompletedAt() == null).count(),
                actionItems.stream().filter(item -> item.getCompletedAt() != null).count(),
                conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).size(),
                dailyReflectionRepository.findTop30ByUserIdOrderByCreatedAtDesc(user.getId()).size(),
                projectApplicationRepository.findByProjectOwnerIdOrderByUpdatedAtDesc(user.getId()).size(),
                reviewSessionRepository.countByUserId(user.getId()),
                reviewSessionRepository.countByUserIdAndCompletedAtIsNotNull(user.getId()),
                totalMinutes,
                mostActiveBooks(user.getId(), readingSessions));
    }

    @Transactional(readOnly = true)
    public KnowledgeAnalyticsResponse knowledgeAnalytics(String email) {
        User user = userService.getByEmailRequired(email);
        List<Concept> concepts = conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId());
        return new KnowledgeAnalyticsResponse(
                concepts.size(),
                knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).size(),
                masteryRepository.countByUserId(user.getId()),
                masteryRepository.countByUserIdAndNextReviewAtBefore(user.getId(), Instant.now()),
                reviewItemRepository.countByReviewSessionUserId(user.getId()),
                reviewItemRepository.countByReviewSessionUserIdAndStatus(user.getId(), "COMPLETED"),
                concepts.stream()
                        .sorted(Comparator.comparing(Concept::getMentionCount, Comparator.nullsLast(Comparator.reverseOrder())))
                        .limit(8)
                        .map(concept -> new AnalyticsCountResponse(concept.getName(), concept.getMentionCount() == null ? 0 : concept.getMentionCount()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public BookAnalyticsResponse bookAnalytics(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, bookId);
        List<ActionItem> actions = actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId);
        List<ReadingSession> sessions = readingSessionRepository.findByUserIdAndBookIdOrderByStartedAtDesc(user.getId(), bookId);
        long reviewItems = reviewSessionRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .filter(session -> "BOOK".equals(session.getScopeType()) && Objects.equals(session.getScopeId(), bookId))
                .mapToLong(session -> reviewItemRepository.findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(session.getId(), user.getId()).size())
                .sum();
        return new BookAnalyticsResponse(
                book.getId(),
                book.getTitle(),
                bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId()).size(),
                rawCaptureRepository.findByUserIdAndBookIdOrderByCreatedAtDesc(user.getId(), bookId).size(),
                quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId).size(),
                actions.size(),
                actions.stream().filter(item -> item.getCompletedAt() != null).count(),
                conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId).size(),
                sessions.size(),
                sessions.stream().map(ReadingSession::getMinutesRead).filter(Objects::nonNull).mapToLong(Integer::longValue).sum(),
                reviewItems);
    }

    private ReviewSession createGeneratedSession(User user, String scopeType, Long scopeId, String title, String mode, String fallbackTitle) {
        ReviewSession session = new ReviewSession();
        session.setUser(user);
        session.setTitle(StringUtils.hasText(title) ? title.trim() : fallbackTitle);
        session.setStartedAt(Instant.now());
        session.setMode(defaultString(mode, "SOURCE_REVIEW"));
        session.setScopeType(scopeType);
        session.setScopeId(scopeId);
        return reviewSessionRepository.save(session);
    }

    private ReviewItem reviewItem(ReviewSession session, String targetType, Long targetId, SourceReference sourceReference, String prompt) {
        ReviewItem item = new ReviewItem();
        item.setReviewSession(session);
        item.setTargetType(targetType);
        item.setTargetId(targetId);
        item.setSourceReference(sourceReference);
        item.setPrompt(prompt);
        return item;
    }

    private void completeSessionIfDone(ReviewSession session, Long userId) {
        List<ReviewItem> items = reviewItemRepository.findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(session.getId(), userId);
        if (!items.isEmpty() && items.stream().allMatch(item -> "COMPLETED".equals(item.getStatus()))) {
            session.setCompletedAt(Instant.now());
            reviewSessionRepository.save(session);
        }
    }

    private KnowledgeMastery upsertMastery(User user, String targetType, Long targetId, Integer familiarity, Integer usefulness, SourceReference sourceReference) {
        String normalizedType = normalizeType(targetType);
        KnowledgeMastery mastery = masteryRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), normalizedType, targetId)
                .orElseGet(() -> {
                    KnowledgeMastery created = new KnowledgeMastery();
                    created.setUser(user);
                    created.setTargetType(normalizedType);
                    created.setTargetId(targetId);
                    return created;
                });
        if (familiarity != null) {
            mastery.setFamiliarityScore(familiarity);
        }
        if (usefulness != null) {
            mastery.setUsefulnessScore(usefulness);
        }
        mastery.setLastReviewedAt(Instant.now());
        if (sourceReference != null) {
            mastery.setSourceReference(sourceReference);
        }
        return masteryRepository.save(mastery);
    }

    private SourceReference ownedSource(User user, Long sourceReferenceId) {
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
    }

    private SourceReference sourceById(User user, Long sourceReferenceId) {
        if (sourceReferenceId == null) {
            return null;
        }
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId()).orElse(null);
    }

    private void assertTargetReadable(User user, String targetType, Long targetId) {
        switch (normalizeType(targetType)) {
            case "BOOK" -> assertBookReadable(user, targetId);
            case "QUOTE" -> quoteRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Quote not found."));
            case "ACTION_ITEM" -> actionItemRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Action item not found."));
            case "CONCEPT" -> conceptRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Concept not found."));
            case "KNOWLEDGE_OBJECT" -> knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
            case "PROJECT", "GAME_PROJECT" -> projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Project not found."));
            case "PROJECT_APPLICATION" -> projectApplicationRepository.findByIdAndProjectOwnerId(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Project application not found."));
            case "PLAYTEST_FINDING" -> playtestFindingRepository.findByIdAndProjectOwnerId(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Playtest finding not found."));
            case "PROJECT_LENS_REVIEW" -> projectLensReviewRepository.findByIdAndProjectOwnerId(targetId, user.getId()).orElseThrow(() -> new NoSuchElementException("Project lens review not found."));
            default -> throw new IllegalArgumentException("Unsupported mastery/review target type: " + targetType);
        }
    }

    private void assertBookReadable(User user, Long bookId) {
        Book book = bookService.getBookEntity(bookId);
        boolean readable = book.getVisibility() == com.bookos.backend.common.enums.Visibility.PUBLIC
                || book.getVisibility() == com.bookos.backend.common.enums.Visibility.SHARED
                || book.getOwner() != null && Objects.equals(book.getOwner().getId(), user.getId())
                || user.getRole() != null && user.getRole().getName() == com.bookos.backend.common.enums.RoleName.ADMIN;
        if (!readable) {
            throw new NoSuchElementException("Book not found.");
        }
    }

    private List<AnalyticsCountResponse> mostActiveBooks(Long userId, List<ReadingSession> sessions) {
        Map<String, Long> counts = new LinkedHashMap<>();
        bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                .forEach(note -> increment(counts, note.getBook().getTitle()));
        rawCaptureRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(capture -> increment(counts, capture.getBook().getTitle()));
        quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                .forEach(quote -> increment(counts, quote.getBook().getTitle()));
        actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                .forEach(action -> increment(counts, action.getBook().getTitle()));
        sessions.forEach(session -> increment(counts, session.getBook().getTitle()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(entry -> new AnalyticsCountResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void increment(Map<String, Long> counts, String label) {
        counts.put(label, counts.getOrDefault(label, 0L) + 1L);
    }

    private ReadingSessionResponse toReadingSessionResponse(ReadingSession session) {
        return new ReadingSessionResponse(
                session.getId(),
                session.getBook().getId(),
                session.getBook().getTitle(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getStartPage(),
                session.getEndPage(),
                session.getMinutesRead(),
                session.getNotesCount(),
                session.getCapturesCount(),
                session.getReflection(),
                session.getCreatedAt(),
                session.getUpdatedAt());
    }

    private ReviewSessionResponse toReviewSessionResponse(Long userId, ReviewSession session, boolean includeItems) {
        List<ReviewItemResponse> items = includeItems
                ? reviewItemRepository.findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(session.getId(), userId).stream()
                        .map(this::toReviewItemResponse)
                        .toList()
                : List.of();
        long completed = items.stream().filter(item -> "COMPLETED".equals(item.status())).count();
        return new ReviewSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getMode(),
                session.getScopeType(),
                session.getScopeId(),
                session.getSummary(),
                includeItems ? items.size() : reviewItemRepository.findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(session.getId(), userId).size(),
                includeItems ? (int) completed : (int) reviewItemRepository.findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(session.getId(), userId).stream().filter(item -> "COMPLETED".equals(item.getStatus())).count(),
                items,
                session.getCreatedAt(),
                session.getUpdatedAt());
    }

    private ReviewItemResponse toReviewItemResponse(ReviewItem item) {
        return new ReviewItemResponse(
                item.getId(),
                item.getReviewSession().getId(),
                item.getTargetType(),
                item.getTargetId(),
                item.getSourceReference() == null ? null : sourceReferenceService.toResponse(item.getSourceReference()),
                item.getPrompt(),
                item.getUserResponse(),
                item.getStatus(),
                item.getConfidenceScore(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }

    private KnowledgeMasteryResponse toMasteryResponse(KnowledgeMastery mastery) {
        return new KnowledgeMasteryResponse(
                mastery.getId(),
                mastery.getTargetType(),
                mastery.getTargetId(),
                mastery.getFamiliarityScore(),
                mastery.getUsefulnessScore(),
                mastery.getLastReviewedAt(),
                mastery.getNextReviewAt(),
                mastery.getSourceReference() == null ? null : sourceReferenceService.toResponse(mastery.getSourceReference()),
                mastery.getCreatedAt(),
                mastery.getUpdatedAt());
    }

    private Integer resolveMinutes(ReadingSession session, Integer explicitMinutes) {
        if (explicitMinutes != null) {
            return nonNegative(explicitMinutes);
        }
        Instant end = session.getEndedAt() == null ? Instant.now() : session.getEndedAt();
        return Math.max(0, (int) Duration.between(session.getStartedAt(), end).toMinutes());
    }

    private int generationLimit(Integer limit) {
        if (limit == null) {
            return 8;
        }
        return Math.max(1, Math.min(12, limit));
    }

    private Integer nonNegative(Integer value) {
        return value == null ? null : Math.max(0, value);
    }

    private Integer nonNegativeOrDefault(Integer value, Integer fallback) {
        return value == null ? fallback : Math.max(0, value);
    }

    private String normalizeType(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : fallback;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String excerpt(String text) {
        if (text == null || text.length() <= 180) {
            return text == null ? "" : text;
        }
        return text.substring(0, 177) + "...";
    }
}
