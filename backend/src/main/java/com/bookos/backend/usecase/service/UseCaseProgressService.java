package com.bookos.backend.usecase.service;

import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.ai.repository.AISuggestionRepository;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.demo.service.DemoWorkspaceService;
import com.bookos.backend.forum.repository.ForumCommentRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.learning.repository.ReviewSessionRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.usecase.dto.UseCaseProgressResponse;
import com.bookos.backend.usecase.dto.UseCaseStepVerificationResponse;
import com.bookos.backend.usecase.entity.UseCaseEventType;
import com.bookos.backend.usecase.entity.UseCaseProgressStatus;
import com.bookos.backend.usecase.entity.UserUseCaseProgress;
import com.bookos.backend.usecase.repository.UserUseCaseEventRepository;
import com.bookos.backend.usecase.repository.UserUseCaseProgressRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UseCaseProgressService {

    private static final Pattern SAFE_STEP_KEY = Pattern.compile("[a-zA-Z0-9_-]{1,120}");
    private static final String KEY_SEPARATOR = "\n";

    private static final Map<String, Set<String>> REQUIRED_STEP_KEYS = Map.ofEntries(
            Map.entry("first-15-minutes", ordered("add-book", "capture", "process", "open-source")),
            Map.entry("track-book-start-to-finish", ordered("create-book", "add-library", "set-reading", "update-progress")),
            Map.entry("reader-mode-track-book", ordered("create-book", "add-library", "set-reading", "update-progress")),
            Map.entry("note-taker-capture-convert", ordered("capture", "convert-note", "convert-quote", "convert-action")),
            Map.entry("apply-quote-to-game-project", ordered("quote", "project", "project-application")),
            Map.entry("game-designer-apply-knowledge", ordered("quote", "project", "project-application", "design-decision")),
            Map.entry("review-concept-marker", ordered("concept-capture", "review-concept", "open-concept")),
            Map.entry("researcher-review-concept", ordered("concept-capture", "review-concept", "open-concept", "start-review")),
            Map.entry("source-linked-forum-discussion", ordered("choose-source", "create-thread", "add-comment")),
            Map.entry("community-source-discussion", ordered("choose-source", "create-thread", "add-comment")),
            Map.entry("advanced-mode-search-graph-export-ai", ordered("search", "graph", "export", "ai-draft")));

    private final UserService userService;
    private final UserUseCaseProgressRepository progressRepository;
    private final UserUseCaseEventRepository eventRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final BookNoteRepository noteRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DesignDecisionRepository designDecisionRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final AISuggestionRepository aiSuggestionRepository;
    private final ReviewSessionRepository reviewSessionRepository;
    private final DemoWorkspaceService demoWorkspaceService;

    @Transactional(readOnly = true)
    public List<UseCaseProgressResponse> list(String email) {
        User user = userService.getByEmailRequired(email);
        List<UseCaseProgressResponse> responses = new ArrayList<>(progressRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(progress -> toResponse(user.getId(), progress.getUseCaseSlug(), progress))
                .toList());
        for (String slug : REQUIRED_STEP_KEYS.keySet()) {
            boolean alreadyPresent = responses.stream().anyMatch(response -> response.useCaseSlug().equals(slug));
            if (!alreadyPresent) {
                UseCaseProgressResponse response = toResponse(user.getId(), slug, null);
                if (!response.automaticCompletedStepKeys().isEmpty()) {
                    responses.add(response);
                }
            }
        }
        responses.sort(Comparator.comparing(UseCaseProgressResponse::useCaseSlug));
        return responses;
    }

    @Transactional(readOnly = true)
    public UseCaseProgressResponse get(String email, String slug) {
        User user = userService.getByEmailRequired(email);
        return toResponse(user.getId(), normalizeSlug(slug), find(user.getId(), slug));
    }

    @Transactional
    public UseCaseProgressResponse start(String email, String slug) {
        User user = userService.getByEmailRequired(email);
        String normalizedSlug = normalizeSlug(slug);
        UserUseCaseProgress progress = find(user.getId(), normalizedSlug);
        Instant now = Instant.now();
        if (progress == null) {
            progress = new UserUseCaseProgress();
            progress.setUser(user);
            progress.setUseCaseSlug(normalizedSlug);
            progress.setCompletedStepKeys("");
        }
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(now);
        }
        if (progress.getStatus() == UseCaseProgressStatus.NOT_STARTED) {
            progress.setStatus(UseCaseProgressStatus.IN_PROGRESS);
        }
        return toResponse(user.getId(), normalizedSlug, progressRepository.save(progress));
    }

    @Transactional
    public UseCaseProgressResponse completeStep(String email, String slug, String stepKey) {
        User user = userService.getByEmailRequired(email);
        String normalizedSlug = normalizeSlug(slug);
        String normalizedStepKey = normalizeStepKey(stepKey);
        UserUseCaseProgress progress = find(user.getId(), normalizedSlug);
        if (progress == null) {
            progress = new UserUseCaseProgress();
            progress.setUser(user);
            progress.setUseCaseSlug(normalizedSlug);
            progress.setStartedAt(Instant.now());
        }
        Set<String> completed = parseKeys(progress.getCompletedStepKeys());
        completed.add(normalizedStepKey);
        progress.setCompletedStepKeys(joinKeys(completed));
        progress.setCurrentStep(Math.max(progress.getCurrentStep(), completed.size()));
        progress.setStatus(UseCaseProgressStatus.IN_PROGRESS);

        Set<String> effective = new LinkedHashSet<>(completed);
        effective.addAll(automaticKeys(normalizedSlug, signals(user.getId())));
        if (isComplete(normalizedSlug, effective)) {
            progress.setStatus(UseCaseProgressStatus.COMPLETED);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(Instant.now());
            }
        }
        return toResponse(user.getId(), normalizedSlug, progressRepository.save(progress));
    }

    @Transactional
    public UseCaseProgressResponse reset(String email, String slug) {
        User user = userService.getByEmailRequired(email);
        String normalizedSlug = normalizeSlug(slug);
        progressRepository.deleteByUserIdAndUseCaseSlug(user.getId(), normalizedSlug);
        return toResponse(user.getId(), normalizedSlug, null);
    }

    private UserUseCaseProgress find(Long userId, String slug) {
        return progressRepository.findByUserIdAndUseCaseSlug(userId, normalizeSlug(slug)).orElse(null);
    }

    private UseCaseProgressResponse toResponse(Long userId, String slug, UserUseCaseProgress progress) {
        Set<String> completed = progress == null ? new LinkedHashSet<>() : parseKeys(progress.getCompletedStepKeys());
        UseCaseSignals signals = signals(userId);
        Set<String> automatic = automaticKeys(slug, signals);
        Set<String> effective = new LinkedHashSet<>(completed);
        effective.addAll(automatic);
        Map<String, UseCaseStepVerificationResponse> stepVerification = stepVerification(slug, completed, automatic, signals);
        UseCaseProgressStatus status = progress == null ? UseCaseProgressStatus.NOT_STARTED : progress.getStatus();
        if (progress != null && isComplete(slug, effective)) {
            status = UseCaseProgressStatus.COMPLETED;
        }
        return new UseCaseProgressResponse(
                slug,
                status,
                progress == null ? 0 : progress.getCurrentStep(),
                completed,
                automatic,
                effective,
                stepVerification,
                progress == null ? null : progress.getStartedAt(),
                progress == null ? null : progress.getCompletedAt(),
                progress == null ? null : progress.getUpdatedAt());
    }

    private Set<String> automaticKeys(String slug, UseCaseSignals signals) {
        Set<String> keys = new LinkedHashSet<>();
        switch (slug) {
            case "first-15-minutes" -> {
                addIf(keys, "add-book", signals.hasBookOrLibrary());
                addIf(keys, "capture", signals.hasCapture());
                addIf(keys, "process", signals.hasProcessedCapture());
                addIf(keys, "open-source", signals.hasOpenedSource());
            }
            case "track-book-start-to-finish", "reader-mode-track-book" -> {
                addIf(keys, "create-book", signals.hasBookOrLibrary());
                addIf(keys, "add-library", signals.hasLibraryBook());
                addIf(keys, "set-reading", signals.hasCurrentlyReading());
                addIf(keys, "update-progress", signals.hasReadingProgress());
            }
            case "note-taker-capture-convert" -> {
                addIf(keys, "capture", signals.hasCapture());
                addIf(keys, "convert-note", signals.hasNote());
                addIf(keys, "convert-quote", signals.hasQuote());
                addIf(keys, "convert-action", signals.hasActionItem());
            }
            case "apply-quote-to-game-project", "game-designer-apply-knowledge" -> {
                addIf(keys, "quote", signals.hasQuote());
                addIf(keys, "project", signals.hasProject());
                addIf(keys, "project-application", signals.hasProjectApplication());
                addIf(keys, "design-decision", signals.hasDesignDecision());
            }
            case "review-concept-marker", "researcher-review-concept" -> {
                addIf(keys, "concept-capture", signals.hasCapture());
                addIf(keys, "review-concept", signals.hasConcept());
                addIf(keys, "open-concept", signals.hasConcept());
                addIf(keys, "start-review", signals.hasReviewSession());
            }
            case "source-linked-forum-discussion", "community-source-discussion" -> {
                addIf(keys, "choose-source", signals.hasSourceContext());
                addIf(keys, "create-thread", signals.hasForumThread());
                addIf(keys, "add-comment", signals.hasForumComment());
            }
            case "advanced-mode-search-graph-export-ai" -> {
                addIf(keys, "search", signals.hasUsedSearch());
                addIf(keys, "graph", signals.hasOpenedGraph());
                addIf(keys, "export", signals.hasStartedExport());
                addIf(keys, "ai-draft", signals.hasAiDraft());
            }
            default -> {
            }
        }
        return keys;
    }

    private Map<String, UseCaseStepVerificationResponse> stepVerification(
            String slug,
            Set<String> manualKeys,
            Set<String> automaticKeys,
            UseCaseSignals signals) {
        Map<String, UseCaseStepVerificationResponse> verification = new LinkedHashMap<>();
        for (String stepKey : REQUIRED_STEP_KEYS.getOrDefault(slug, Set.of())) {
            verification.put(stepKey, stepVerification(slug, stepKey, manualKeys, automaticKeys, signals));
        }
        return verification;
    }

    private UseCaseStepVerificationResponse stepVerification(
            String slug,
            String stepKey,
            Set<String> manualKeys,
            Set<String> automaticKeys,
            UseCaseSignals signals) {
        if (automaticKeys.contains(stepKey)) {
            return new UseCaseStepVerificationResponse(stepKey, "auto", true, true, false, "Verified automatically from real user-owned records or events.");
        }
        if (manualKeys.contains(stepKey)) {
            return new UseCaseStepVerificationResponse(stepKey, "manual", true, false, true, "Marked complete manually by the user.");
        }
        StepRule rule = stepRule(slug, stepKey, signals);
        if (!rule.available()) {
            return new UseCaseStepVerificationResponse(stepKey, "unavailable", false, false, false, rule.message());
        }
        if (!rule.prerequisiteMet()) {
            return new UseCaseStepVerificationResponse(stepKey, "missingPrerequisite", false, false, false, rule.message());
        }
        return new UseCaseStepVerificationResponse(stepKey, "blocked", false, false, false, rule.message());
    }

    private StepRule stepRule(String slug, String stepKey, UseCaseSignals signals) {
        return switch (stepKey) {
            case "add-book", "create-book" ->
                    new StepRule(true, true, "Add or open a real non-demo book to complete this step.");
            case "add-library" ->
                    new StepRule(true, signals.hasBookOrLibrary(), "Add a real book to your personal library.");
            case "set-reading" ->
                    new StepRule(true, signals.hasLibraryBook(), "Set a non-demo library book to Currently Reading.");
            case "update-progress" ->
                    new StepRule(true, signals.hasLibraryBook(), "Update progress or rating on a non-demo library book.");
            case "capture", "concept-capture" ->
                    new StepRule(true, signals.hasBookOrLibrary(), "Save a real non-demo capture with a book selected.");
            case "process" ->
                    new StepRule(true, signals.hasCapture(), "Convert a capture, create a note, create a quote, create an action, or review a concept.");
            case "convert-note" ->
                    new StepRule(true, signals.hasCapture(), "Convert a real capture to a note or create a real note.");
            case "convert-quote", "quote" ->
                    new StepRule(true, signals.hasCapture() || signals.hasBookOrLibrary(), "Create a real quote or convert a capture to quote.");
            case "convert-action" ->
                    new StepRule(true, signals.hasCapture() || signals.hasBookOrLibrary(), "Create a real action or convert a capture to action.");
            case "open-source" ->
                    new StepRule(true, signals.hasSourceContext(), "Open Source from a source-backed record.");
            case "project" ->
                    new StepRule(true, true, "Create a real non-demo game project.");
            case "project-application" ->
                    new StepRule(true, signals.hasProject() && signals.hasSourceContext(), "Apply a source-backed record to a real project.");
            case "design-decision" ->
                    new StepRule(true, signals.hasProject(), "Create a design decision inside a real project.");
            case "review-concept", "open-concept" ->
                    new StepRule(true, signals.hasCapture() || signals.hasConcept(), "Review a parsed concept marker or create a real concept.");
            case "start-review" ->
                    new StepRule(true, signals.hasConcept(), "Start a review session from a real concept or book.");
            case "choose-source" ->
                    new StepRule(true, signals.hasBookOrLibrary() || signals.hasSourceContext(), "Choose a real quote, concept, source link, book, or knowledge object.");
            case "create-thread" ->
                    new StepRule(true, signals.hasBookOrLibrary() || signals.hasSourceContext(), "Create a real forum thread from accessible context.");
            case "add-comment" ->
                    new StepRule(true, signals.hasForumThread(), "Add a real comment to a forum thread.");
            case "search" ->
                    new StepRule(true, signals.hasAnyUserRecord(), "Use global search after creating at least one real record.");
            case "graph" ->
                    new StepRule(true, signals.hasSourceContext() || signals.hasProject(), "Open the graph after creating source-backed records, relationships, or project context.");
            case "export" ->
                    new StepRule(true, signals.hasAnyUserRecord(), "Start an export after creating at least one real record.");
            case "ai-draft" ->
                    new StepRule(true, signals.hasAnyUserRecord(), "Generate or review a draft assistant suggestion from real content.");
            default ->
                    new StepRule(false, false, "This step is manual-only because no safe automatic detector exists yet.");
        };
    }

    private UseCaseSignals signals(Long userId) {
        Set<Long> demoBooks = demoWorkspaceService.demoIds(userId, "BOOK");
        Set<Long> demoUserBooks = demoWorkspaceService.demoIds(userId, "USER_BOOK");
        Set<Long> demoCaptures = demoWorkspaceService.demoIds(userId, "RAW_CAPTURE");

        long ownedBooks = subtractDemo(bookRepository.countByOwnerId(userId), demoBooks);
        List<UserBook> realUserBooks = userBookRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(userBook -> !demoUserBooks.contains(userBook.getId()))
                .toList();
        long libraryBooks = realUserBooks.size();
        long currentlyReading = realUserBooks.stream()
                .filter(userBook -> userBook.getReadingStatus() == ReadingStatus.CURRENTLY_READING)
                .count();
        long progressUpdated = realUserBooks.stream()
                .filter(userBook -> (userBook.getProgressPercent() != null && userBook.getProgressPercent() > 0)
                        || userBook.getRating() != null)
                .count();

        List<RawCapture> realCaptures = rawCaptureRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(capture -> !demoCaptures.contains(capture.getId()))
                .toList();
        long captures = realCaptures.size();
        long unprocessedCaptures = realCaptures.stream()
                .filter(capture -> capture.getStatus() == CaptureStatus.INBOX)
                .count();
        long convertedCaptures = realCaptures.stream()
                .filter(capture -> capture.getStatus() == CaptureStatus.CONVERTED)
                .count();

        long notes = noteRepository.countByUserIdAndArchivedFalse(userId);
        long quotes = subtractDemo(quoteRepository.countByUserIdAndArchivedFalse(userId), demoWorkspaceService.demoIds(userId, "QUOTE"));
        long actions = subtractDemo(actionItemRepository.countByUserIdAndArchivedFalse(userId), demoWorkspaceService.demoIds(userId, "ACTION_ITEM"));
        long concepts = subtractDemo(conceptRepository.countByUserIdAndArchivedFalse(userId), demoWorkspaceService.demoIds(userId, "CONCEPT"));
        long knowledgeObjects = subtractDemo(knowledgeObjectRepository.countByUserIdAndArchivedFalse(userId), demoWorkspaceService.demoIds(userId, "KNOWLEDGE_OBJECT"));
        long sources = subtractDemo(sourceReferenceRepository.countByUserId(userId), demoWorkspaceService.demoIds(userId, "SOURCE_REFERENCE"));
        long projects = subtractDemo(projectRepository.countByOwnerIdAndArchivedAtIsNull(userId), demoWorkspaceService.demoIds(userId, "GAME_PROJECT"));
        long applications = subtractDemo(projectApplicationRepository.countByProjectOwnerId(userId), demoWorkspaceService.demoIds(userId, "PROJECT_APPLICATION"));
        long designDecisions = subtractDemo(designDecisionRepository.countByProjectOwnerId(userId), demoWorkspaceService.demoIds(userId, "DESIGN_DECISION"));
        long threads = subtractDemo(forumThreadRepository.countByAuthorIdAndStatusNot(userId, ForumThreadStatus.HIDDEN), demoWorkspaceService.demoIds(userId, "FORUM_THREAD"));
        long comments = subtractDemo(forumCommentRepository.countByAuthorIdAndArchivedFalse(userId), demoWorkspaceService.demoIds(userId, "FORUM_COMMENT"));
        long aiDrafts = aiSuggestionRepository.countByUserId(userId);
        long reviewSessions = reviewSessionRepository.countByUserId(userId);
        long sourceOpenEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.SOURCE_OPENED);
        long searchEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.SEARCH_USED);
        long graphEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.GRAPH_OPENED);
        long exportEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.EXPORT_STARTED);
        return new UseCaseSignals(ownedBooks, libraryBooks, currentlyReading, progressUpdated, captures, unprocessedCaptures,
                convertedCaptures, notes, quotes, actions, concepts, knowledgeObjects, sources, projects, applications,
                designDecisions, threads, comments, aiDrafts, reviewSessions, sourceOpenEvents, searchEvents, graphEvents,
                exportEvents);
    }

    private static long subtractDemo(long count, Set<Long> demoIds) {
        return Math.max(0, count - demoIds.size());
    }

    private boolean isComplete(String slug, Set<String> effectiveKeys) {
        Set<String> requiredKeys = REQUIRED_STEP_KEYS.get(slug);
        return requiredKeys != null && effectiveKeys.containsAll(requiredKeys);
    }

    private static void addIf(Set<String> keys, String key, boolean condition) {
        if (condition) {
            keys.add(key);
        }
    }

    private static Set<String> parseKeys(String value) {
        Set<String> keys = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return keys;
        }
        Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .forEach(keys::add);
        return keys;
    }

    private static String joinKeys(Set<String> keys) {
        return String.join(KEY_SEPARATOR, keys);
    }

    private static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank() || slug.length() > 120) {
            throw new IllegalArgumentException("Use case slug is required.");
        }
        return slug.trim();
    }

    private static String normalizeStepKey(String stepKey) {
        if (stepKey == null || !SAFE_STEP_KEY.matcher(stepKey.trim()).matches()) {
            throw new IllegalArgumentException("Step key is invalid.");
        }
        return stepKey.trim();
    }

    private static Set<String> ordered(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }

    private record UseCaseSignals(
            long ownedBooks,
            long libraryBooks,
            long currentlyReading,
            long progressUpdated,
            long captures,
            long unprocessedCaptures,
            long convertedCaptures,
            long notes,
            long quotes,
            long actions,
            long concepts,
            long knowledgeObjects,
            long sources,
            long projects,
            long applications,
            long designDecisions,
            long threads,
            long comments,
            long aiDrafts,
            long reviewSessions,
            long sourceOpenEvents,
            long searchEvents,
            long graphEvents,
            long exportEvents) {

        boolean hasBookOrLibrary() {
            return ownedBooks > 0 || libraryBooks > 0;
        }

        boolean hasLibraryBook() {
            return libraryBooks > 0;
        }

        boolean hasCurrentlyReading() {
            return currentlyReading > 0;
        }

        boolean hasReadingProgress() {
            return progressUpdated > 0;
        }

        boolean hasCapture() {
            return captures > 0;
        }

        boolean hasUnprocessedCapture() {
            return unprocessedCaptures > 0;
        }

        boolean hasProcessedCapture() {
            return convertedCaptures > 0 || notes > 0 || quotes > 0 || actions > 0 || concepts > 0;
        }

        boolean hasNote() {
            return notes > 0;
        }

        boolean hasQuote() {
            return quotes > 0;
        }

        boolean hasActionItem() {
            return actions > 0;
        }

        boolean hasConcept() {
            return concepts > 0;
        }

        boolean hasSourceContext() {
            return sources > 0 || quotes > 0 || concepts > 0 || knowledgeObjects > 0;
        }

        boolean hasProject() {
            return projects > 0;
        }

        boolean hasProjectApplication() {
            return applications > 0;
        }

        boolean hasDesignDecision() {
            return designDecisions > 0;
        }

        boolean hasForumThread() {
            return threads > 0;
        }

        boolean hasForumComment() {
            return comments > 0;
        }

        boolean hasAiDraft() {
            return aiDrafts > 0;
        }

        boolean hasReviewSession() {
            return reviewSessions > 0;
        }

        boolean hasAnyUserRecord() {
            return hasBookOrLibrary() || hasCapture() || hasNote() || hasQuote() || hasActionItem()
                    || hasConcept() || knowledgeObjects > 0 || sources > 0 || hasProject()
                    || hasProjectApplication() || hasForumThread() || hasReviewSession();
        }

        boolean hasOpenedSource() {
            return sourceOpenEvents > 0;
        }

        boolean hasUsedSearch() {
            return searchEvents > 0;
        }

        boolean hasOpenedGraph() {
            return graphEvents > 0;
        }

        boolean hasStartedExport() {
            return exportEvents > 0;
        }
    }

    private record StepRule(boolean available, boolean prerequisiteMet, String message) {}
}
