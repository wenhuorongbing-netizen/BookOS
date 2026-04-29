package com.bookos.backend.ai.service;

import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.ai.dto.AISuggestionEditRequest;
import com.bookos.backend.ai.dto.AISuggestionRequest;
import com.bookos.backend.ai.dto.AISuggestionResponse;
import com.bookos.backend.ai.dto.AIProviderStatusResponse;
import com.bookos.backend.ai.entity.AIInteraction;
import com.bookos.backend.ai.entity.AISuggestion;
import com.bookos.backend.ai.repository.AIInteractionRepository;
import com.bookos.backend.ai.repository.AISuggestionRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.AISuggestionStatus;
import com.bookos.backend.common.enums.AISuggestionType;
import com.bookos.backend.common.enums.CaptureStatus;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AISuggestionService {

    private final AIProvider aiProvider;
    private final AIInteractionRepository interactionRepository;
    private final AISuggestionRepository suggestionRepository;
    private final UserService userService;
    private final BookService bookService;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final SourceReferenceService sourceReferenceService;
    private final AIProviderRouter aiProviderRouter;
    private final AIProviderProperties aiProviderProperties;
    private final AISuggestionValidator suggestionValidator;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final BookNoteRepository bookNoteRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AISuggestionResponse generate(String email, AISuggestionType type, AISuggestionRequest request) {
        User user = userService.getByEmailRequired(email);
        SourceMaterial sourceMaterial = resolveSourceMaterial(user, request == null ? new AISuggestionRequest(null, null, null, null, null) : request);
        AIDraft draft = aiProvider.generate(type, truncateInput(sourceMaterial.sourceText()), sourceMaterial.sourceTitle());
        draft = suggestionValidator.validate(type, draft, sourceMaterial.sourceReference());

        AIInteraction interaction = new AIInteraction();
        interaction.setUser(user);
        interaction.setProviderName(aiProvider.providerName());
        interaction.setSuggestionType(type);
        interaction.setBook(sourceMaterial.book());
        interaction.setSourceReferenceId(sourceMaterial.sourceReference() == null ? null : sourceMaterial.sourceReference().getId());
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("type", type.name());
        input.put("provider", aiProvider.providerName());
        input.put("bookId", sourceMaterial.book() == null ? null : sourceMaterial.book().getId());
        input.put("sourceReferenceId", sourceMaterial.sourceReference() == null ? null : sourceMaterial.sourceReference().getId());
        input.put("maxInputChars", aiProviderProperties.getMaxInputChars());
        interaction.setInputJson(writeJson(input));
        interaction = interactionRepository.save(interaction);

        AISuggestion suggestion = new AISuggestion();
        suggestion.setUser(user);
        suggestion.setInteraction(interaction);
        suggestion.setProviderName(aiProvider.providerName());
        suggestion.setSuggestionType(type);
        suggestion.setStatus(AISuggestionStatus.DRAFT);
        suggestion.setBook(sourceMaterial.book());
        suggestion.setSourceReferenceId(sourceMaterial.sourceReference() == null ? null : sourceMaterial.sourceReference().getId());
        suggestion.setDraftText(draft.draftText());
        suggestion.setDraftJson(draft.draftJson());
        return toResponse(suggestionRepository.save(suggestion));
    }

    @Transactional(readOnly = true)
    public AIProviderStatusResponse providerStatus() {
        AIProviderStatus status = aiProviderRouter.status();
        return new AIProviderStatusResponse(
                status.enabled(),
                status.available(),
                status.configuredProvider(),
                status.activeProvider(),
                status.externalProviderConfigured(),
                status.maxInputChars(),
                status.message());
    }

    @Transactional(readOnly = true)
    public List<AISuggestionResponse> listSuggestions(String email) {
        User user = userService.getByEmailRequired(email);
        return suggestionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AISuggestionResponse acceptSuggestion(String email, Long id) {
        AISuggestion suggestion = getOwnedSuggestion(email, id);
        ensureDraft(suggestion);
        suggestion.setStatus(AISuggestionStatus.ACCEPTED);
        suggestion.setAcceptedAt(Instant.now());
        return toResponse(suggestionRepository.save(suggestion));
    }

    @Transactional
    public AISuggestionResponse rejectSuggestion(String email, Long id) {
        AISuggestion suggestion = getOwnedSuggestion(email, id);
        ensureDraft(suggestion);
        suggestion.setStatus(AISuggestionStatus.REJECTED);
        suggestion.setRejectedAt(Instant.now());
        return toResponse(suggestionRepository.save(suggestion));
    }

    @Transactional
    public AISuggestionResponse editSuggestion(String email, Long id, AISuggestionEditRequest request) {
        AISuggestion suggestion = getOwnedSuggestion(email, id);
        ensureDraft(suggestion);
        if (request == null) {
            throw new IllegalArgumentException("Draft edit payload is required.");
        }
        if (StringUtils.hasText(request.draftText())) {
            if (request.draftText().trim().length() > 8000) {
                throw new IllegalArgumentException("AI draft text is too long.");
            }
            suggestion.setDraftText(request.draftText().trim());
        }
        if (StringUtils.hasText(request.draftJson())) {
            SourceReference sourceReference = suggestion.getSourceReferenceId() == null
                    ? null
                    : sourceReferenceRepository.findByIdAndUserId(suggestion.getSourceReferenceId(), suggestion.getUser().getId()).orElse(null);
            AIDraft validated = suggestionValidator.validate(
                    suggestion.getSuggestionType(),
                    new AIDraft(suggestion.getDraftText(), request.draftJson().trim()),
                    sourceReference);
            suggestion.setDraftJson(validated.draftJson());
        }
        return toResponse(suggestionRepository.save(suggestion));
    }

    private AISuggestion getOwnedSuggestion(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        return suggestionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("AI suggestion not found."));
    }

    private void ensureDraft(AISuggestion suggestion) {
        if (suggestion.getStatus() != AISuggestionStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft AI suggestions can be changed.");
        }
    }

    private SourceMaterial resolveSourceMaterial(User user, AISuggestionRequest request) {
        if (request.sourceReferenceId() != null) {
            SourceReference source = sourceReferenceRepository.findByIdAndUserId(request.sourceReferenceId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
            return new SourceMaterial(source.getBook(), source, source.getSourceText(), source.getBook().getTitle());
        }

        Book requestedBook = request.bookId() == null ? null : bookService.getAccessibleBookEntity(user.getEmail(), request.bookId());

        if (request.noteId() != null) {
            BookNote note = bookNoteRepository.findByIdAndUserId(request.noteId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Note not found."));
            if (requestedBook != null && !requestedBook.getId().equals(note.getBook().getId())) {
                throw new IllegalArgumentException("Note belongs to a different book.");
            }
            return new SourceMaterial(
                    note.getBook(),
                    null,
                    firstText(note.getThreeSentenceSummary(), note.getMarkdown()),
                    note.getTitle());
        }

        if (request.rawCaptureId() != null) {
            var capture = rawCaptureRepository.findByIdAndUserId(request.rawCaptureId(), user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Capture not found."));
            if (requestedBook != null && !requestedBook.getId().equals(capture.getBook().getId())) {
                throw new IllegalArgumentException("Capture belongs to a different book.");
            }
            return new SourceMaterial(
                    capture.getBook(),
                    null,
                    firstText(capture.getCleanText(), capture.getRawText()),
                    capture.getBook().getTitle());
        }

        String requestText = StringUtils.hasText(request.text()) ? request.text().trim() : null;
        if (requestText != null) {
            return new SourceMaterial(requestedBook, null, requestText, requestedBook == null ? "User selected text" : requestedBook.getTitle());
        }

        Long bookId = requestedBook == null ? null : requestedBook.getId();
        String sourceText = latestOwnedSourceText(user, bookId);
        return new SourceMaterial(requestedBook, null, sourceText, requestedBook == null ? "Recent BookOS content" : requestedBook.getTitle());
    }

    private String latestOwnedSourceText(User user, Long bookId) {
        if (bookId != null) {
            return firstText(
                    quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId).stream()
                            .findFirst()
                            .map(quote -> quote.getText())
                            .orElse(null),
                    actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId).stream()
                            .findFirst()
                            .map(item -> firstText(item.getDescription(), item.getTitle()))
                            .orElse(null),
                    rawCaptureRepository.findByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(user.getId(), bookId, CaptureStatus.INBOX).stream()
                            .findFirst()
                            .map(capture -> firstText(capture.getCleanText(), capture.getRawText()))
                            .orElse(null),
                    bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId()).stream()
                            .findFirst()
                            .map(note -> firstText(note.getThreeSentenceSummary(), note.getMarkdown()))
                            .orElse(null));
        }

        return firstText(
                quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                        .findFirst()
                        .map(quote -> quote.getText())
                        .orElse(null),
                actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                        .findFirst()
                        .map(item -> firstText(item.getDescription(), item.getTitle()))
                        .orElse(null),
                rawCaptureRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CaptureStatus.INBOX).stream()
                        .findFirst()
                        .map(capture -> firstText(capture.getCleanText(), capture.getRawText()))
                        .orElse(null),
                bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                        .findFirst()
                        .map(note -> firstText(note.getThreeSentenceSummary(), note.getMarkdown()))
                        .orElse(null));
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String truncateInput(String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return sourceText;
        }
        int maxChars = Math.max(1000, aiProviderProperties.getMaxInputChars());
        String clean = sourceText.replaceAll("\\s+", " ").trim();
        return clean.length() <= maxChars ? clean : clean.substring(0, maxChars);
    }

    private String writeJson(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.entrySet().removeIf(entry -> entry.getValue() == null);
        try {
            return objectMapper.writeValueAsString(cleaned);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("AI interaction input could not be serialized.", exception);
        }
    }

    private AISuggestionResponse toResponse(AISuggestion suggestion) {
        SourceReference sourceReference = suggestion.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(suggestion.getSourceReferenceId(), suggestion.getUser().getId()).orElse(null);
        return new AISuggestionResponse(
                suggestion.getId(),
                suggestion.getSuggestionType(),
                suggestion.getStatus(),
                suggestion.getProviderName(),
                suggestion.getBook() == null ? null : suggestion.getBook().getId(),
                suggestion.getBook() == null ? null : suggestion.getBook().getTitle(),
                suggestion.getSourceReferenceId(),
                sourceReference == null ? null : sourceReferenceService.toResponse(sourceReference),
                suggestion.getDraftText(),
                suggestion.getDraftJson(),
                suggestion.getCreatedAt(),
                suggestion.getUpdatedAt());
    }

    private record SourceMaterial(Book book, SourceReference sourceReference, String sourceText, String sourceTitle) {}
}
