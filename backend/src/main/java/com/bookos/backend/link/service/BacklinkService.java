package com.bookos.backend.link.service;

import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.dto.BacklinkResponse;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
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
public class BacklinkService {

    private final EntityLinkRepository entityLinkRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final SourceReferenceService sourceReferenceService;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final BookNoteRepository bookNoteRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BacklinkResponse> listBacklinks(String email, String entityType, Long entityId) {
        User user = userService.getByEmailRequired(email);
        String type = normalize(entityType);
        resolveEntity(user, type, entityId);

        Map<String, BacklinkResponse> backlinks = new LinkedHashMap<>();
        entityLinkRepository.findByUserIdAndSourceTypeAndSourceIdOrderByCreatedAtDesc(user.getId(), type, entityId)
                .forEach(link -> toBacklink(user, link, "OUTGOING", link.getTargetType(), link.getTargetId())
                        .ifPresent(backlink -> backlinks.putIfAbsent(backlinkKey(backlink), backlink)));
        entityLinkRepository.findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(user.getId(), type, entityId)
                .forEach(link -> toBacklink(user, link, "INCOMING", link.getSourceType(), link.getSourceId())
                        .ifPresent(backlink -> backlinks.putIfAbsent(backlinkKey(backlink), backlink)));

        return List.copyOf(backlinks.values());
    }

    private java.util.Optional<BacklinkResponse> toBacklink(User user, EntityLink link, String direction, String type, Long id) {
        EntitySummary summary;
        try {
            summary = resolveEntity(user, normalize(type), id);
        } catch (RuntimeException ignored) {
            return java.util.Optional.empty();
        }
        SourceReference source = link.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(link.getSourceReferenceId(), user.getId()).orElse(null);
        return java.util.Optional.of(new BacklinkResponse(
                link.getId(),
                direction,
                link.getRelationType(),
                normalize(type),
                id,
                summary.title(),
                summary.excerpt(),
                source == null ? null : sourceReferenceService.toResponse(source),
                link.getCreatedAt()));
    }

    private EntitySummary resolveEntity(User user, String type, Long id) {
        if (id == null) {
            throw new NoSuchElementException("Entity not found.");
        }

        return switch (type) {
            case "BOOK" -> resolveBook(user, id);
            case "NOTE" -> bookNoteRepository.findByIdAndUserId(id, user.getId())
                    .map(note -> new EntitySummary(note.getTitle(), excerpt(note.getThreeSentenceSummary(), note.getMarkdown())))
                    .orElseThrow(() -> new NoSuchElementException("Note not found."));
            case "RAW_CAPTURE", "CAPTURE" -> rawCaptureRepository.findByIdAndUserId(id, user.getId())
                    .map(capture -> new EntitySummary("Capture #" + capture.getId(), excerpt(capture.getCleanText(), capture.getRawText())))
                    .orElseThrow(() -> new NoSuchElementException("Capture not found."));
            case "QUOTE" -> quoteRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .map(quote -> new EntitySummary("Quote from " + quote.getBook().getTitle(), excerpt(quote.getText())))
                    .orElseThrow(() -> new NoSuchElementException("Quote not found."));
            case "ACTION_ITEM" -> actionItemRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .map(item -> new EntitySummary(item.getTitle(), excerpt(item.getDescription(), item.getTitle())))
                    .orElseThrow(() -> new NoSuchElementException("Action item not found."));
            case "CONCEPT" -> conceptRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .map(concept -> new EntitySummary(concept.getName(), excerpt(concept.getDescription())))
                    .orElseThrow(() -> new NoSuchElementException("Concept not found."));
            case "KNOWLEDGE_OBJECT" -> knowledgeObjectRepository.findByIdAndUserIdAndArchivedFalse(id, user.getId())
                    .map(object -> new EntitySummary(object.getTitle(), excerpt(object.getDescription())))
                    .orElseThrow(() -> new NoSuchElementException("Knowledge object not found."));
            case "SOURCE_REFERENCE" -> sourceReferenceRepository.findByIdAndUserId(id, user.getId())
                    .map(source -> new EntitySummary(sourceLabel(source), excerpt(source.getSourceText())))
                    .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
            case "FORUM_THREAD" -> resolveForumThread(user, id);
            case "DAILY_PROMPT", "DAILY_DESIGN_PROMPT" -> dailyDesignPromptRepository.findByIdAndUserId(id, user.getId())
                    .map(prompt -> new EntitySummary("Daily prompt", excerpt(prompt.getQuestion())))
                    .orElseThrow(() -> new NoSuchElementException("Daily design prompt not found."));
            default -> throw new IllegalArgumentException("Unsupported backlink entity type: " + type);
        };
    }

    private EntitySummary resolveBook(User user, Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Book not found."));
        if (canReadBook(user, book)) {
            return new EntitySummary(book.getTitle(), excerpt(book.getSubtitle(), book.getDescription()));
        }
        throw new AccessDeniedException("You are not allowed to inspect backlinks for this book.");
    }

    private EntitySummary resolveForumThread(User user, Long id) {
        ForumThread thread = forumThreadRepository.findByIdAndStatusNot(id, ForumThreadStatus.ARCHIVED)
                .orElseThrow(() -> new NoSuchElementException("Forum thread not found."));
        if (Objects.equals(thread.getAuthor().getId(), user.getId())
                || thread.getVisibility() == Visibility.PUBLIC
                || thread.getVisibility() == Visibility.SHARED) {
            return new EntitySummary(thread.getTitle(), excerpt(thread.getBodyMarkdown()));
        }
        throw new NoSuchElementException("Forum thread not found.");
    }

    private boolean canReadBook(User user, Book book) {
        if (book.getVisibility() == Visibility.PUBLIC || book.getVisibility() == Visibility.SHARED) {
            return true;
        }
        if (book.getOwner() != null && Objects.equals(book.getOwner().getId(), user.getId())) {
            return true;
        }
        return userBookRepository.findByUserIdAndBookId(user.getId(), book.getId()).isPresent();
    }

    private String sourceLabel(SourceReference source) {
        if (StringUtils.hasText(source.getLocationLabel())) {
            return source.getLocationLabel();
        }
        if (source.getPageStart() != null && source.getPageEnd() != null) {
            return "p." + source.getPageStart() + "-" + source.getPageEnd();
        }
        if (source.getPageStart() != null) {
            return "p." + source.getPageStart();
        }
        return source.getSourceType();
    }

    private String excerpt(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                String clean = value.replaceAll("\\s+", " ").trim();
                return clean.length() > 220 ? clean.substring(0, 217) + "..." : clean;
            }
        }
        return null;
    }

    private String backlinkKey(BacklinkResponse backlink) {
        return backlink.direction() + ":" + backlink.relationType() + ":" + backlink.entityType() + ":" + backlink.entityId();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private record EntitySummary(String title, String excerpt) {}
}
