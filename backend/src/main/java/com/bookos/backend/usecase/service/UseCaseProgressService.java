package com.bookos.backend.usecase.service;

import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.ai.repository.AISuggestionRepository;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.forum.repository.ForumCommentRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.usecase.dto.UseCaseProgressResponse;
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

    private static final Map<String, Set<String>> REQUIRED_STEP_KEYS = Map.of(
            "first-15-minutes", ordered("add-book", "capture", "process", "open-source"),
            "track-book-start-to-finish", ordered("create-book", "add-library", "set-reading", "update-progress"),
            "note-taker-capture-convert", ordered("capture", "convert-note", "convert-quote", "convert-action"),
            "apply-quote-to-game-project", ordered("quote", "project", "project-application"),
            "review-concept-marker", ordered("concept-capture", "review-concept", "open-concept"),
            "source-linked-forum-discussion", ordered("choose-source", "create-thread", "add-comment"),
            "advanced-mode-search-graph-export-ai", ordered("search", "graph", "export", "ai-draft"));

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
    private final ForumThreadRepository forumThreadRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final AISuggestionRepository aiSuggestionRepository;

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
        effective.addAll(automaticKeys(user.getId(), normalizedSlug));
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
        Set<String> automatic = automaticKeys(userId, slug);
        Set<String> effective = new LinkedHashSet<>(completed);
        effective.addAll(automatic);
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
                progress == null ? null : progress.getStartedAt(),
                progress == null ? null : progress.getCompletedAt(),
                progress == null ? null : progress.getUpdatedAt());
    }

    private Set<String> automaticKeys(Long userId, String slug) {
        UseCaseSignals signals = signals(userId);
        Set<String> keys = new LinkedHashSet<>();
        switch (slug) {
            case "first-15-minutes" -> {
                addIf(keys, "add-book", signals.hasBookOrLibrary());
                addIf(keys, "capture", signals.hasCapture());
                addIf(keys, "process", signals.hasProcessedCapture());
                addIf(keys, "open-source", signals.hasOpenedSource());
            }
            case "track-book-start-to-finish" -> {
                addIf(keys, "create-book", signals.hasBookOrLibrary());
                addIf(keys, "add-library", signals.hasLibraryBook());
            }
            case "note-taker-capture-convert" -> {
                addIf(keys, "capture", signals.hasCapture());
                addIf(keys, "convert-note", signals.hasNote());
                addIf(keys, "convert-quote", signals.hasQuote());
                addIf(keys, "convert-action", signals.hasActionItem());
            }
            case "apply-quote-to-game-project" -> {
                addIf(keys, "quote", signals.hasQuote());
                addIf(keys, "project", signals.hasProject());
                addIf(keys, "project-application", signals.hasProjectApplication());
            }
            case "review-concept-marker" -> {
                addIf(keys, "concept-capture", signals.hasCapture());
                addIf(keys, "review-concept", signals.hasConcept());
                addIf(keys, "open-concept", signals.hasConcept());
            }
            case "source-linked-forum-discussion" -> {
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

    private UseCaseSignals signals(Long userId) {
        long ownedBooks = bookRepository.countByOwnerId(userId);
        long libraryBooks = userBookRepository.countByUserId(userId);
        long captures = rawCaptureRepository.countByUserId(userId);
        long convertedCaptures = rawCaptureRepository.countByUserIdAndStatus(userId, CaptureStatus.CONVERTED);
        long notes = noteRepository.countByUserIdAndArchivedFalse(userId);
        long quotes = quoteRepository.countByUserIdAndArchivedFalse(userId);
        long actions = actionItemRepository.countByUserIdAndArchivedFalse(userId);
        long concepts = conceptRepository.countByUserIdAndArchivedFalse(userId);
        long knowledgeObjects = knowledgeObjectRepository.countByUserIdAndArchivedFalse(userId);
        long sources = sourceReferenceRepository.countByUserId(userId);
        long projects = projectRepository.countByOwnerIdAndArchivedAtIsNull(userId);
        long applications = projectApplicationRepository.countByProjectOwnerId(userId);
        long threads = forumThreadRepository.countByAuthorIdAndStatusNot(userId, ForumThreadStatus.HIDDEN);
        long comments = forumCommentRepository.countByAuthorIdAndArchivedFalse(userId);
        long aiDrafts = aiSuggestionRepository.countByUserId(userId);
        long sourceOpenEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.SOURCE_OPENED);
        long searchEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.SEARCH_USED);
        long graphEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.GRAPH_OPENED);
        long exportEvents = eventRepository.countByUserIdAndEventType(userId, UseCaseEventType.EXPORT_STARTED);
        return new UseCaseSignals(ownedBooks, libraryBooks, captures, convertedCaptures, notes, quotes, actions, concepts,
                knowledgeObjects, sources, projects, applications, threads, comments, aiDrafts, sourceOpenEvents,
                searchEvents, graphEvents, exportEvents);
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
            long captures,
            long convertedCaptures,
            long notes,
            long quotes,
            long actions,
            long concepts,
            long knowledgeObjects,
            long sources,
            long projects,
            long applications,
            long threads,
            long comments,
            long aiDrafts,
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

        boolean hasCapture() {
            return captures > 0;
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

        boolean hasForumThread() {
            return threads > 0;
        }

        boolean hasForumComment() {
            return comments > 0;
        }

        boolean hasAiDraft() {
            return aiDrafts > 0;
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
}
