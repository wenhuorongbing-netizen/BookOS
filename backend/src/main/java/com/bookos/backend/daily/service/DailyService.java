package com.bookos.backend.daily.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.daily.dto.CreatePrototypeTaskRequest;
import com.bookos.backend.daily.dto.DailyDesignPromptResponse;
import com.bookos.backend.daily.dto.DailyHistoryResponse;
import com.bookos.backend.daily.dto.DailyReflectionRequest;
import com.bookos.backend.daily.dto.DailyReflectionResponse;
import com.bookos.backend.daily.dto.DailySentenceResponse;
import com.bookos.backend.daily.dto.DailyTarget;
import com.bookos.backend.daily.dto.DailyTodayResponse;
import com.bookos.backend.daily.entity.DailyDesignPrompt;
import com.bookos.backend.daily.entity.DailyHistory;
import com.bookos.backend.daily.entity.DailyReflection;
import com.bookos.backend.daily.entity.DailySentence;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.daily.repository.DailyHistoryRepository;
import com.bookos.backend.daily.repository.DailyReflectionRepository;
import com.bookos.backend.daily.repository.DailySentenceRepository;
import com.bookos.backend.knowledge.dto.KnowledgeObjectRequest;
import com.bookos.backend.knowledge.dto.KnowledgeObjectResponse;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.knowledge.service.KnowledgeObjectService;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.note.repository.NoteBlockRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DailyService {

    private static final List<KnowledgeObjectType> PROMPT_TYPES = List.of(
            KnowledgeObjectType.DESIGN_LENS,
            KnowledgeObjectType.DIAGNOSTIC_QUESTION,
            KnowledgeObjectType.EXERCISE,
            KnowledgeObjectType.PROTOTYPE_TASK);

    private final DailySentenceRepository dailySentenceRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final DailyReflectionRepository dailyReflectionRepository;
    private final DailyHistoryRepository dailyHistoryRepository;
    private final QuoteRepository quoteRepository;
    private final NoteBlockRepository noteBlockRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final UserBookRepository userBookRepository;
    private final UserService userService;
    private final SourceReferenceService sourceReferenceService;
    private final KnowledgeObjectService knowledgeObjectService;

    @Transactional
    public DailyTodayResponse getToday(String email) {
        User user = userService.getByEmailRequired(email);
        LocalDate day = LocalDate.now();
        DailySentence sentence = getOrCreateSentence(user, day);
        DailyDesignPrompt prompt = getOrCreatePrompt(user, day);
        return todayResponse(user, day, sentence, prompt);
    }

    @Transactional
    public DailyTodayResponse regenerate(String email, DailyTarget target) {
        User user = userService.getByEmailRequired(email);
        LocalDate day = LocalDate.now();
        if (target == DailyTarget.SENTENCE) {
            regenerateSentence(user, day);
        } else {
            regeneratePrompt(user, day);
        }
        return todayResponse(
                user,
                day,
                dailySentenceRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day).orElse(null),
                dailyDesignPromptRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day).orElse(null));
    }

    @Transactional
    public DailyTodayResponse skip(String email, DailyTarget target) {
        User user = userService.getByEmailRequired(email);
        LocalDate day = LocalDate.now();
        if (target == DailyTarget.SENTENCE) {
            DailySentence current = dailySentenceRepository
                    .findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day)
                    .orElse(null);
            if (current != null) {
                current.setActive(false);
                current.setSkipped(true);
                dailySentenceRepository.save(current);
                recordHistory(user, day, DailyTarget.SENTENCE, "SKIPPED", current.getSourceType(), current.getSourceId(), current.getId(), null);
            }
            getOrCreateSentence(user, day);
        } else {
            DailyDesignPrompt current = dailyDesignPromptRepository
                    .findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day)
                    .orElse(null);
            if (current != null) {
                current.setActive(false);
                current.setSkipped(true);
                dailyDesignPromptRepository.save(current);
                recordHistory(user, day, DailyTarget.PROMPT, "SKIPPED", current.getSourceType(), current.getSourceId(), null, current.getId());
            }
            getOrCreatePrompt(user, day);
        }
        return todayResponse(
                user,
                day,
                dailySentenceRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day).orElse(null),
                dailyDesignPromptRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day).orElse(null));
    }

    @Transactional
    public DailyReflectionResponse saveReflection(String email, DailyReflectionRequest request) {
        User user = userService.getByEmailRequired(email);
        LocalDate day = LocalDate.now();
        DailyReflection reflection = new DailyReflection();
        reflection.setUser(user);
        reflection.setDay(day);
        reflection.setTargetType(request.target().name());
        reflection.setReflectionText(request.reflectionText().trim());

        if (request.target() == DailyTarget.SENTENCE) {
            DailySentence sentence = request.dailySentenceId() == null
                    ? getOrCreateSentence(user, day)
                    : dailySentenceRepository.findByIdAndUserId(request.dailySentenceId(), user.getId())
                            .orElseThrow(() -> new NoSuchElementException("Daily sentence not found."));
            if (sentence == null) {
                throw new NoSuchElementException("Daily sentence not found.");
            }
            reflection.setDailySentenceId(sentence.getId());
        } else {
            DailyDesignPrompt prompt = request.dailyDesignPromptId() == null
                    ? getOrCreatePrompt(user, day)
                    : dailyDesignPromptRepository.findByIdAndUserId(request.dailyDesignPromptId(), user.getId())
                            .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));
            reflection.setDailyDesignPromptId(prompt.getId());
        }

        return toReflectionResponse(dailyReflectionRepository.save(reflection));
    }

    @Transactional(readOnly = true)
    public List<DailyHistoryResponse> history(String email) {
        User user = userService.getByEmailRequired(email);
        return dailyHistoryRepository.findTop50ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public KnowledgeObjectResponse createPrototypeTask(String email, CreatePrototypeTaskRequest request) {
        User user = userService.getByEmailRequired(email);
        LocalDate day = LocalDate.now();
        DailyDesignPrompt prompt = request.dailyDesignPromptId() == null
                ? getOrCreatePrompt(user, day)
                : dailyDesignPromptRepository.findByIdAndUserId(request.dailyDesignPromptId(), user.getId())
                        .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));

        String baseTitle = StringUtils.hasText(request.title())
                ? request.title().trim()
                : "Prototype task: " + abbreviate(prompt.getQuestion(), 120);
        String description = StringUtils.hasText(request.description())
                ? request.description().trim()
                : "Created from the daily design prompt:\n\n" + prompt.getQuestion();

        KnowledgeObjectRequest payload = new KnowledgeObjectRequest(
                KnowledgeObjectType.PROTOTYPE_TASK,
                baseTitle,
                description,
                Visibility.PRIVATE,
                prompt.getBook() == null ? null : prompt.getBook().getId(),
                null,
                null,
                prompt.getSourceReferenceId(),
                null,
                List.of("daily", "prototype"));

        try {
            return knowledgeObjectService.createKnowledgeObject(email, payload);
        } catch (IllegalArgumentException duplicate) {
            KnowledgeObjectRequest deduped = new KnowledgeObjectRequest(
                    KnowledgeObjectType.PROTOTYPE_TASK,
                    baseTitle + " - " + day,
                    description,
                    Visibility.PRIVATE,
                    payload.bookId(),
                    null,
                    null,
                    payload.sourceReferenceId(),
                    payload.ontologyLayer(),
                    payload.tags());
            return knowledgeObjectService.createKnowledgeObject(email, deduped);
        }
    }

    private DailySentence getOrCreateSentence(User user, LocalDate day) {
        return dailySentenceRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day)
                .orElseGet(() -> createSentence(user, day, historyKeys(user, day, DailyTarget.SENTENCE)));
    }

    private DailyDesignPrompt getOrCreatePrompt(User user, LocalDate day) {
        return dailyDesignPromptRepository.findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(user.getId(), day)
                .orElseGet(() -> createPrompt(user, day, historyKeys(user, day, DailyTarget.PROMPT)));
    }

    private DailySentence createSentence(User user, LocalDate day, Set<String> excludedKeys) {
        List<SentenceCandidate> candidates = sentenceCandidates(user, day);
        SentenceCandidate selected = selectCandidate(candidates, excludedKeys);
        if (selected == null && !excludedKeys.isEmpty()) {
            selected = selectCandidate(candidates, Set.of());
        }
        if (selected == null) {
            return null;
        }

        DailySentence sentence = new DailySentence();
        sentence.setUser(user);
        sentence.setDay(day);
        sentence.setBook(selected.book());
        sentence.setSourceType(selected.sourceType());
        sentence.setSourceId(selected.sourceId());
        sentence.setSourceReferenceId(selected.sourceReference() == null ? null : selected.sourceReference().getId());
        sentence.setText(selected.text());
        sentence.setAttribution(selected.attribution());
        sentence.setPageStart(selected.pageStart());
        sentence.setPageEnd(selected.pageEnd());
        sentence.setSourceBacked(selected.sourceBacked());
        DailySentence saved = dailySentenceRepository.save(sentence);
        recordHistory(user, day, DailyTarget.SENTENCE, "SELECTED", saved.getSourceType(), saved.getSourceId(), saved.getId(), null);
        return saved;
    }

    private DailyDesignPrompt createPrompt(User user, LocalDate day, Set<String> excludedKeys) {
        List<PromptCandidate> candidates = promptCandidates(user, day);
        PromptCandidate selected = selectCandidate(candidates, excludedKeys);
        if (selected == null && !excludedKeys.isEmpty()) {
            selected = selectCandidate(candidates, Set.of());
        }

        DailyDesignPrompt prompt = new DailyDesignPrompt();
        prompt.setUser(user);
        prompt.setDay(day);
        prompt.setBook(selected.book());
        prompt.setKnowledgeObject(selected.knowledgeObject());
        prompt.setSourceType(selected.sourceType());
        prompt.setSourceId(selected.sourceId());
        prompt.setSourceReferenceId(selected.sourceReference() == null ? null : selected.sourceReference().getId());
        prompt.setQuestion(selected.question());
        prompt.setSourceTitle(selected.sourceTitle());
        prompt.setTemplatePrompt(selected.templatePrompt());
        DailyDesignPrompt saved = dailyDesignPromptRepository.save(prompt);
        recordHistory(user, day, DailyTarget.PROMPT, "SELECTED", saved.getSourceType(), saved.getSourceId(), null, saved.getId());
        return saved;
    }

    private void regenerateSentence(User user, LocalDate day) {
        DailySentence current = getOrCreateSentence(user, day);
        if (current == null) {
            return;
        }
        Set<String> excluded = historyKeys(user, day, DailyTarget.SENTENCE);
        excluded.add(key(current.getSourceType(), current.getSourceId()));
        SentenceCandidate replacement = selectCandidate(sentenceCandidates(user, day), excluded);
        if (replacement == null) {
            return;
        }

        current.setActive(false);
        dailySentenceRepository.save(current);
        recordHistory(user, day, DailyTarget.SENTENCE, "REGENERATED", current.getSourceType(), current.getSourceId(), current.getId(), null);
        createSentence(user, day, excluded);
    }

    private void regeneratePrompt(User user, LocalDate day) {
        DailyDesignPrompt current = getOrCreatePrompt(user, day);
        Set<String> excluded = historyKeys(user, day, DailyTarget.PROMPT);
        excluded.add(key(current.getSourceType(), current.getSourceId()));
        PromptCandidate replacement = selectCandidate(promptCandidates(user, day), excluded);
        if (replacement == null) {
            return;
        }

        current.setActive(false);
        dailyDesignPromptRepository.save(current);
        recordHistory(user, day, DailyTarget.PROMPT, "REGENERATED", current.getSourceType(), current.getSourceId(), null, current.getId());
        createPrompt(user, day, excluded);
    }

    private List<SentenceCandidate> sentenceCandidates(User user, LocalDate day) {
        Map<Long, UserBook> libraryState = libraryState(user);
        List<SentenceCandidate> candidates = new ArrayList<>();

        for (Quote quote : quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())) {
            SourceReference source = findSource(user, quote.getSourceReferenceId());
            String text = trimToNull(quote.getText());
            if (text == null) {
                continue;
            }
            candidates.add(new SentenceCandidate(
                    key("QUOTE", quote.getId()),
                    "QUOTE",
                    quote.getId(),
                    quote.getBook(),
                    source,
                    text,
                    quote.getAttribution(),
                    source != null ? source.getPageStart() : quote.getPageStart(),
                    source != null ? source.getPageEnd() : quote.getPageEnd(),
                    true,
                    score(quote.getBook(), source, quote.getUpdatedAt(), libraryState, day, user.getId(), "QUOTE", quote.getId())));
        }

        for (NoteBlock block : noteBlockRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())) {
            String text = abbreviate(trimToNull(block.getPlainText()) == null ? block.getRawText() : block.getPlainText(), 520);
            if (!StringUtils.hasText(text)) {
                continue;
            }
            SourceReference source = block.getSourceReferences().stream().findFirst().orElse(null);
            candidates.add(new SentenceCandidate(
                    key("NOTE_BLOCK", block.getId()),
                    "NOTE_BLOCK",
                    block.getId(),
                    block.getBook(),
                    source,
                    text,
                    block.getNote().getTitle(),
                    source != null ? source.getPageStart() : block.getPageStart(),
                    source != null ? source.getPageEnd() : block.getPageEnd(),
                    source != null,
                    score(block.getBook(), source, block.getUpdatedAt(), libraryState, day, user.getId(), "NOTE_BLOCK", block.getId())));
        }

        for (RawCapture capture : rawCaptureRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CaptureStatus.CONVERTED)) {
            String convertedType = trimToNull(capture.getConvertedEntityType());
            if (convertedType == null || (!convertedType.equals("NOTE") && !convertedType.equals("QUOTE"))) {
                continue;
            }
            String text = abbreviate(trimToNull(capture.getCleanText()) == null ? capture.getRawText() : capture.getCleanText(), 520);
            if (!StringUtils.hasText(text)) {
                continue;
            }
            SourceReference source = sourceReferenceRepository
                    .findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(capture.getId(), user.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            candidates.add(new SentenceCandidate(
                    key("RAW_CAPTURE", capture.getId()),
                    "RAW_CAPTURE",
                    capture.getId(),
                    capture.getBook(),
                    source,
                    text,
                    capture.getBook().getTitle(),
                    source != null ? source.getPageStart() : capture.getPageStart(),
                    source != null ? source.getPageEnd() : capture.getPageEnd(),
                    source != null,
                    score(capture.getBook(), source, capture.getUpdatedAt(), libraryState, day, user.getId(), "RAW_CAPTURE", capture.getId())));
        }

        return candidates.stream()
                .sorted(Comparator.comparingDouble(SentenceCandidate::score).reversed().thenComparing(SentenceCandidate::key))
                .toList();
    }

    private List<PromptCandidate> promptCandidates(User user, LocalDate day) {
        Map<Long, UserBook> libraryState = libraryState(user);
        List<PromptCandidate> candidates = new ArrayList<>();
        for (KnowledgeObject knowledgeObject : knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId())) {
            if (!PROMPT_TYPES.contains(knowledgeObject.getType())) {
                continue;
            }
            SourceReference source = findSource(user, knowledgeObject.getSourceReferenceId());
            String question = promptQuestion(knowledgeObject);
            candidates.add(new PromptCandidate(
                    key("KNOWLEDGE_OBJECT", knowledgeObject.getId()),
                    "KNOWLEDGE_OBJECT",
                    knowledgeObject.getId(),
                    knowledgeObject.getBook(),
                    knowledgeObject,
                    source,
                    question,
                    knowledgeObject.getTitle(),
                    false,
                    score(knowledgeObject.getBook(), source, knowledgeObject.getUpdatedAt(), libraryState, day, user.getId(), "PROMPT", knowledgeObject.getId())));
        }

        if (!candidates.isEmpty()) {
            return candidates.stream()
                    .sorted(Comparator.comparingDouble(PromptCandidate::score).reversed().thenComparing(PromptCandidate::key))
                    .toList();
        }

        return templatePromptCandidates(user, day);
    }

    private List<PromptCandidate> templatePromptCandidates(User user, LocalDate day) {
        List<String> templates = List.of(
                "What one player decision could you make more meaningful in a current prototype?",
                "Where does feedback arrive too late, too early, or too weakly in your current design?",
                "What is the smallest playtest you could run today to validate one assumption?",
                "Which rule creates the most interesting tension, and how could you amplify it?");
        List<PromptCandidate> candidates = new ArrayList<>();
        for (int index = 0; index < templates.size(); index++) {
            long sourceId = index + 1L;
            candidates.add(new PromptCandidate(
                    key("TEMPLATE", sourceId),
                    "TEMPLATE",
                    sourceId,
                    null,
                    null,
                    null,
                    templates.get(index),
                    "Template Prompt",
                    true,
                    deterministicJitter(user.getId(), day, "TEMPLATE_PROMPT", sourceId)));
        }
        return candidates.stream()
                .sorted(Comparator.comparingDouble(PromptCandidate::score).reversed().thenComparing(PromptCandidate::key))
                .toList();
    }

    private <T extends DailyCandidate> T selectCandidate(List<T> candidates, Set<String> excludedKeys) {
        return candidates.stream()
                .filter(candidate -> !excludedKeys.contains(candidate.key()))
                .findFirst()
                .orElse(null);
    }

    private double score(
            Book book,
            SourceReference source,
            Instant updatedAt,
            Map<Long, UserBook> libraryState,
            LocalDate day,
            Long userId,
            String salt,
            Long sourceId) {
        double score = 10;
        if (book != null) {
            UserBook userBook = libraryState.get(book.getId());
            if (userBook != null) {
                Integer rating = userBook.getRating();
                if (rating != null) {
                    score += rating * 4.0;
                    if (rating >= 5) {
                        score += 10;
                    }
                }
                if (userBook.getReadingStatus() == ReadingStatus.CURRENTLY_READING) {
                    score += 8;
                }
            }
        }
        if (source != null) {
            score += 6;
            if (source.getPageStart() != null) {
                score += 4;
            }
        }
        score += freshnessScore(updatedAt, day);
        score += deterministicJitter(userId, day, salt, sourceId);
        return score;
    }

    private double freshnessScore(Instant updatedAt, LocalDate day) {
        if (updatedAt == null) {
            return 0;
        }
        LocalDate updatedDate = updatedAt.atZone(ZoneOffset.UTC).toLocalDate();
        long days = Math.max(0, ChronoUnit.DAYS.between(updatedDate, day));
        return Math.max(0, 12 - Math.min(12, days));
    }

    private double deterministicJitter(Long userId, LocalDate day, String salt, Long sourceId) {
        return Math.floorMod(Objects.hash(userId, day, salt, sourceId), 1000) / 1000.0;
    }

    private Map<Long, UserBook> libraryState(User user) {
        Map<Long, UserBook> state = new HashMap<>();
        for (UserBook userBook : userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())) {
            state.put(userBook.getBook().getId(), userBook);
        }
        return state;
    }

    private SourceReference findSource(User user, Long sourceReferenceId) {
        if (sourceReferenceId == null) {
            return null;
        }
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId()).orElse(null);
    }

    private String promptQuestion(KnowledgeObject object) {
        String title = object.getTitle();
        return switch (object.getType()) {
            case DESIGN_LENS -> "Apply the design lens \"" + title + "\": what does it reveal about your current prototype?";
            case DIAGNOSTIC_QUESTION, QUESTION -> title.trim().endsWith("?") ? title : "Use this diagnostic question: " + title + "?";
            case EXERCISE -> "What is the smallest version of the exercise \"" + title + "\" you can run today?";
            case PROTOTYPE_TASK -> "What concrete prototype step follows from \"" + title + "\" today?";
            default -> "How can you apply \"" + title + "\" to a current design decision today?";
        };
    }

    private Set<String> historyKeys(User user, LocalDate day, DailyTarget target) {
        Set<String> keys = new LinkedHashSet<>();
        for (DailyHistory history : dailyHistoryRepository.findByUserIdAndDayAndItemTypeOrderByCreatedAtAsc(
                user.getId(), day, target.name())) {
            keys.add(key(history.getSourceType(), history.getSourceId()));
        }
        return keys;
    }

    private void recordHistory(
            User user,
            LocalDate day,
            DailyTarget target,
            String action,
            String sourceType,
            Long sourceId,
            Long dailySentenceId,
            Long dailyDesignPromptId) {
        DailyHistory history = new DailyHistory();
        history.setUser(user);
        history.setDay(day);
        history.setItemType(target.name());
        history.setAction(action);
        history.setSourceType(sourceType);
        history.setSourceId(sourceId);
        history.setDailySentenceId(dailySentenceId);
        history.setDailyDesignPromptId(dailyDesignPromptId);
        dailyHistoryRepository.save(history);
    }

    private DailyTodayResponse todayResponse(User user, LocalDate day, DailySentence sentence, DailyDesignPrompt prompt) {
        return new DailyTodayResponse(
                day,
                sentence == null ? null : toSentenceResponse(sentence),
                prompt == null ? null : toPromptResponse(prompt),
                dailyReflectionRepository.findByUserIdAndDayOrderByCreatedAtDesc(user.getId(), day)
                        .stream()
                        .map(this::toReflectionResponse)
                        .toList());
    }

    private DailySentenceResponse toSentenceResponse(DailySentence sentence) {
        SourceReferenceResponse source = sentence.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(sentence.getSourceReferenceId(), sentence.getUser().getId())
                        .map(sourceReferenceService::toResponse)
                        .orElse(null);
        return new DailySentenceResponse(
                sentence.getId(),
                sentence.getDay(),
                sentence.getText(),
                sentence.getAttribution(),
                sentence.getBook() == null ? null : sentence.getBook().getId(),
                sentence.getBook() == null ? null : sentence.getBook().getTitle(),
                sentence.getSourceType(),
                sentence.getSourceId(),
                sentence.getPageStart(),
                sentence.getPageEnd(),
                sentence.isSourceBacked(),
                sentence.isSkipped(),
                source,
                sentence.getCreatedAt(),
                sentence.getUpdatedAt());
    }

    private DailyDesignPromptResponse toPromptResponse(DailyDesignPrompt prompt) {
        SourceReferenceResponse source = prompt.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(prompt.getSourceReferenceId(), prompt.getUser().getId())
                        .map(sourceReferenceService::toResponse)
                        .orElse(null);
        return new DailyDesignPromptResponse(
                prompt.getId(),
                prompt.getDay(),
                prompt.getQuestion(),
                prompt.getSourceTitle(),
                prompt.getBook() == null ? null : prompt.getBook().getId(),
                prompt.getBook() == null ? null : prompt.getBook().getTitle(),
                prompt.getSourceType(),
                prompt.getSourceId(),
                prompt.getKnowledgeObject() == null ? null : prompt.getKnowledgeObject().getId(),
                prompt.isTemplatePrompt(),
                prompt.isSkipped(),
                source,
                prompt.getCreatedAt(),
                prompt.getUpdatedAt());
    }

    private DailyReflectionResponse toReflectionResponse(DailyReflection reflection) {
        return new DailyReflectionResponse(
                reflection.getId(),
                reflection.getDay(),
                DailyTarget.valueOf(reflection.getTargetType()),
                reflection.getDailySentenceId(),
                reflection.getDailyDesignPromptId(),
                reflection.getReflectionText(),
                reflection.getCreatedAt(),
                reflection.getUpdatedAt());
    }

    private DailyHistoryResponse toHistoryResponse(DailyHistory history) {
        return new DailyHistoryResponse(
                history.getId(),
                history.getDay(),
                DailyTarget.valueOf(history.getItemType()),
                history.getAction(),
                history.getSourceType(),
                history.getSourceId(),
                history.getDailySentenceId(),
                history.getDailyDesignPromptId(),
                history.getCreatedAt());
    }

    private String key(String sourceType, Long sourceId) {
        return (sourceType == null ? "UNKNOWN" : sourceType) + ":" + (sourceId == null ? "NONE" : sourceId);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String abbreviate(String value, int maxLength) {
        String text = trimToNull(value);
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)).trim() + "…";
    }

    private interface DailyCandidate {
        String key();
        double score();
    }

    private record SentenceCandidate(
            String key,
            String sourceType,
            Long sourceId,
            Book book,
            SourceReference sourceReference,
            String text,
            String attribution,
            Integer pageStart,
            Integer pageEnd,
            boolean sourceBacked,
            double score) implements DailyCandidate {}

    private record PromptCandidate(
            String key,
            String sourceType,
            Long sourceId,
            Book book,
            KnowledgeObject knowledgeObject,
            SourceReference sourceReference,
            String question,
            String sourceTitle,
            boolean templatePrompt,
            double score) implements DailyCandidate {}
}
