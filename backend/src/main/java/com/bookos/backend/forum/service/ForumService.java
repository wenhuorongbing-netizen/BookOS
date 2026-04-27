package com.bookos.backend.forum.service;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.repository.BookRepository;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.common.SlugUtils;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.RoleName;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.forum.dto.ForumCategoryRequest;
import com.bookos.backend.forum.dto.ForumCategoryResponse;
import com.bookos.backend.forum.dto.ForumCommentRequest;
import com.bookos.backend.forum.dto.ForumCommentResponse;
import com.bookos.backend.forum.dto.ForumReportRequest;
import com.bookos.backend.forum.dto.ForumThreadRequest;
import com.bookos.backend.forum.dto.ForumThreadResponse;
import com.bookos.backend.forum.dto.StructuredPostTemplateResponse;
import com.bookos.backend.forum.entity.ForumBookmark;
import com.bookos.backend.forum.entity.ForumCategory;
import com.bookos.backend.forum.entity.ForumComment;
import com.bookos.backend.forum.entity.ForumLike;
import com.bookos.backend.forum.entity.ForumReport;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.entity.StructuredPostTemplate;
import com.bookos.backend.forum.repository.ForumBookmarkRepository;
import com.bookos.backend.forum.repository.ForumCategoryRepository;
import com.bookos.backend.forum.repository.ForumCommentRepository;
import com.bookos.backend.forum.repository.ForumLikeRepository;
import com.bookos.backend.forum.repository.ForumReportRepository;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.forum.repository.StructuredPostTemplateRepository;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.source.service.SourceReferenceService;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ForumService {

    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Book Discussions", "Discuss books, chapters, reading paths, and source-backed interpretations."),
            new DefaultCategory("Concept Discussions", "Connect concepts across notes, quotes, books, and design practice."),
            new DefaultCategory("Design Lens Reviews", "Review and improve active design lenses."),
            new DefaultCategory("Prototype Challenges", "Turn reading insights into prototype tasks and constraints."),
            new DefaultCategory("Project Critiques", "Apply book knowledge to game projects and critique decisions."),
            new DefaultCategory("Reading Groups", "Coordinate reading plans, progress, and group reflection."),
            new DefaultCategory("General", "General BookOS discussion that does not fit a structured category."));

    private final ForumCategoryRepository categoryRepository;
    private final ForumThreadRepository threadRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumLikeRepository likeRepository;
    private final ForumBookmarkRepository bookmarkRepository;
    private final ForumReportRepository reportRepository;
    private final StructuredPostTemplateRepository templateRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final ConceptRepository conceptRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final SourceReferenceService sourceReferenceService;
    private final UserService userService;

    @Transactional
    public void ensureDefaults() {
        int index = 0;
        for (DefaultCategory item : DEFAULT_CATEGORIES) {
            String slug = SlugUtils.slugify(item.name());
            if (!categoryRepository.existsBySlug(slug)) {
                ForumCategory category = new ForumCategory();
                category.setName(item.name());
                category.setSlug(slug);
                category.setDescription(item.description());
                category.setSortOrder(index);
                categoryRepository.save(category);
            }
            index += 10;
        }

        seedTemplate("Book Discussion", "book-discussion", "Discuss a book, chapter, or reading claim.", "BOOK",
                "## Reading Context\n\n- Book:\n- Chapter/Page:\n\n## Core Claim\n\nWhat is the idea worth discussing?\n\n## Design Application\n\nHow could this change a project or prototype?");
        seedTemplate("Concept Discussion", "concept-discussion", "Connect a concept to books, notes, or examples.", "CONCEPT",
                "## Concept\n\nWhat does this concept mean in your words?\n\n## Sources\n\nWhich notes, quotes, or books support it?\n\n## Open Question\n\nWhat needs debate?");
        seedTemplate("Quote Discussion", "quote-discussion", "Discuss a source-backed quote without losing its origin.", "QUOTE",
                "## Quote\n\n> Paste or reference the quote.\n\n## Interpretation\n\nWhat do you think it means?\n\n## Design Consequence\n\nWhat should change in practice?");
        seedTemplate("Design Lens Review", "design-lens-review", "Evaluate whether a design lens is useful.", "DESIGN_LENS",
                "## Lens\n\nWhat does this lens help you notice?\n\n## Strength\n\nWhere is it useful?\n\n## Weakness\n\nWhere can it mislead?");
        seedTemplate("Prototype Challenge", "prototype-challenge", "Convert an idea into a playable experiment.", "PROTOTYPE_TASK",
                "## Challenge\n\nWhat should be prototyped?\n\n## Constraint\n\nWhat must stay small?\n\n## Success Signal\n\nWhat would prove the idea works?");
        seedTemplate("Project Critique", "project-critique", "Apply source-backed knowledge to a game project.", "GAME_PROJECT",
                "## Project Context\n\nWhat is being designed?\n\n## Source Insight\n\nWhich book/note/concept informs this critique?\n\n## Critique Request\n\nWhat feedback do you need?");
    }

    @Transactional(readOnly = true)
    public List<ForumCategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional
    public ForumCategoryResponse createCategory(String email, ForumCategoryRequest request) {
        User user = userService.getByEmailRequired(email);
        if (!isModerator(user)) {
            throw new AccessDeniedException("Only moderators can create forum categories.");
        }
        String name = request.name().trim();
        String slug = SlugUtils.slugify(name);
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Forum category already exists.");
        }
        ForumCategory category = new ForumCategory();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(trimToNull(request.description()));
        category.setSortOrder(request.sortOrder() == null ? 100 : request.sortOrder());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<StructuredPostTemplateResponse> listTemplates() {
        return templateRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ForumThreadResponse> listThreads(String email, String categorySlug, String query) {
        User viewer = userService.getByEmailRequired(email);
        String q = StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
        List<ForumThread> threads = StringUtils.hasText(categorySlug)
                ? threadRepository.findByCategorySlugAndStatusNotOrderByUpdatedAtDesc(categorySlug, ForumThreadStatus.ARCHIVED)
                : threadRepository.findByStatusNotOrderByUpdatedAtDesc(ForumThreadStatus.ARCHIVED);
        return threads.stream()
                .filter(thread -> canReadThread(viewer, thread))
                .filter(thread -> q == null
                        || thread.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || thread.getBodyMarkdown().toLowerCase(Locale.ROOT).contains(q))
                .map(thread -> toThreadResponse(viewer, thread))
                .toList();
    }

    @Transactional
    public ForumThreadResponse createThread(String email, ForumThreadRequest request) {
        User author = userService.getByEmailRequired(email);
        ForumThread thread = new ForumThread();
        thread.setAuthor(author);
        applyThreadRequest(author, thread, request);
        return toThreadResponse(author, threadRepository.save(thread));
    }

    @Transactional(readOnly = true)
    public ForumThreadResponse getThread(String email, Long id) {
        User viewer = userService.getByEmailRequired(email);
        ForumThread thread = getExistingThread(id);
        if (!canReadThread(viewer, thread)) {
            throw new NoSuchElementException("Forum thread not found.");
        }
        return toThreadResponse(viewer, thread);
    }

    @Transactional
    public ForumThreadResponse updateThread(String email, Long id, ForumThreadRequest request) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = getExistingThread(id);
        requireCanEdit(user, thread);
        applyThreadRequest(user, thread, request);
        return toThreadResponse(user, threadRepository.save(thread));
    }

    @Transactional
    public void deleteThread(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = getExistingThread(id);
        requireCanEdit(user, thread);
        thread.setStatus(ForumThreadStatus.ARCHIVED);
        threadRepository.save(thread);
    }

    @Transactional(readOnly = true)
    public List<ForumCommentResponse> listComments(String email, Long threadId) {
        User viewer = userService.getByEmailRequired(email);
        ForumThread thread = getExistingThread(threadId);
        if (!canReadThread(viewer, thread)) {
            throw new NoSuchElementException("Forum thread not found.");
        }
        return commentRepository.findByThreadIdAndArchivedFalseOrderByCreatedAtAsc(threadId).stream()
                .map(comment -> toCommentResponse(viewer, comment))
                .toList();
    }

    @Transactional
    public ForumCommentResponse createComment(String email, Long threadId, ForumCommentRequest request) {
        User author = userService.getByEmailRequired(email);
        ForumThread thread = getExistingThread(threadId);
        if (!canReadThread(author, thread)) {
            throw new NoSuchElementException("Forum thread not found.");
        }
        if (thread.getStatus() == ForumThreadStatus.CLOSED) {
            throw new IllegalArgumentException("This thread is closed.");
        }
        ForumComment comment = new ForumComment();
        comment.setAuthor(author);
        comment.setThread(thread);
        comment.setBodyMarkdown(sanitizeMarkdown(request.bodyMarkdown()));
        comment.setParentCommentId(request.parentCommentId());
        return toCommentResponse(author, commentRepository.save(comment));
    }

    @Transactional
    public ForumCommentResponse updateComment(String email, Long id, ForumCommentRequest request) {
        User user = userService.getByEmailRequired(email);
        ForumComment comment = getExistingComment(id);
        requireCanEdit(user, comment);
        comment.setBodyMarkdown(sanitizeMarkdown(request.bodyMarkdown()));
        return toCommentResponse(user, commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(String email, Long id) {
        User user = userService.getByEmailRequired(email);
        ForumComment comment = getExistingComment(id);
        requireCanEdit(user, comment);
        comment.setArchived(true);
        commentRepository.save(comment);
    }

    @Transactional
    public ForumThreadResponse bookmark(String email, Long threadId) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = readableThread(user, threadId);
        bookmarkRepository.findByThreadIdAndUserId(threadId, user.getId()).orElseGet(() -> {
            ForumBookmark bookmark = new ForumBookmark();
            bookmark.setThread(thread);
            bookmark.setUser(user);
            return bookmarkRepository.save(bookmark);
        });
        return toThreadResponse(user, thread);
    }

    @Transactional
    public ForumThreadResponse removeBookmark(String email, Long threadId) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = readableThread(user, threadId);
        bookmarkRepository.findByThreadIdAndUserId(threadId, user.getId()).ifPresent(bookmarkRepository::delete);
        return toThreadResponse(user, thread);
    }

    @Transactional
    public ForumThreadResponse like(String email, Long threadId) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = readableThread(user, threadId);
        likeRepository.findByThreadIdAndUserId(threadId, user.getId()).orElseGet(() -> {
            ForumLike like = new ForumLike();
            like.setThread(thread);
            like.setUser(user);
            return likeRepository.save(like);
        });
        return toThreadResponse(user, thread);
    }

    @Transactional
    public ForumThreadResponse removeLike(String email, Long threadId) {
        User user = userService.getByEmailRequired(email);
        ForumThread thread = readableThread(user, threadId);
        likeRepository.findByThreadIdAndUserId(threadId, user.getId()).ifPresent(likeRepository::delete);
        return toThreadResponse(user, thread);
    }

    @Transactional
    public void report(String email, Long threadId, ForumReportRequest request) {
        User reporter = userService.getByEmailRequired(email);
        ForumThread thread = readableThread(reporter, threadId);
        ForumReport report = new ForumReport();
        report.setThread(thread);
        report.setReporter(reporter);
        report.setReason(request.reason().trim());
        report.setDetails(trimToNull(request.details()));
        reportRepository.save(report);
    }

    private void seedTemplate(String name, String slug, String description, String type, String body) {
        if (templateRepository.existsBySlug(slug)) {
            return;
        }
        StructuredPostTemplate template = new StructuredPostTemplate();
        template.setName(name);
        template.setSlug(slug);
        template.setDescription(description);
        template.setDefaultRelatedEntityType(type);
        template.setBodyMarkdownTemplate(body);
        template.setSortOrder((int) templateRepository.count() * 10);
        templateRepository.save(template);
    }

    private ForumThread readableThread(User user, Long id) {
        ForumThread thread = getExistingThread(id);
        if (!canReadThread(user, thread)) {
            throw new NoSuchElementException("Forum thread not found.");
        }
        return thread;
    }

    private ForumThread getExistingThread(Long id) {
        return threadRepository.findByIdAndStatusNot(id, ForumThreadStatus.ARCHIVED)
                .orElseThrow(() -> new NoSuchElementException("Forum thread not found."));
    }

    private ForumComment getExistingComment(Long id) {
        return commentRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new NoSuchElementException("Forum comment not found."));
    }

    private void applyThreadRequest(User author, ForumThread thread, ForumThreadRequest request) {
        ForumCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NoSuchElementException("Forum category not found."));
        SourceReference sourceReference = request.sourceReferenceId() == null ? null : getOwnedSourceReference(author, request.sourceReferenceId());
        Book book = request.relatedBookId() == null ? null : getReadableBook(author, request.relatedBookId());
        Concept concept = request.relatedConceptId() == null ? null : getOwnedConcept(author, request.relatedConceptId());

        if (sourceReference != null) {
            book = sourceReference.getBook();
        }
        if (concept != null && book == null) {
            book = concept.getFirstBook();
        }

        thread.setCategory(category);
        thread.setTitle(request.title().trim());
        thread.setBodyMarkdown(sanitizeMarkdown(request.bodyMarkdown()));
        thread.setRelatedEntityType(normalizeEntityType(request.relatedEntityType()));
        thread.setRelatedEntityId(request.relatedEntityId());
        thread.setRelatedBook(book);
        thread.setRelatedConcept(concept);
        thread.setSourceReferenceId(sourceReference == null ? null : sourceReference.getId());
        thread.setVisibility(request.visibility() == null ? Visibility.SHARED : request.visibility());
    }

    private SourceReference getOwnedSourceReference(User user, Long sourceReferenceId) {
        return sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Source reference not found."));
    }

    private Book getReadableBook(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found."));
        if (canReadBook(user, book)) {
            return book;
        }
        throw new AccessDeniedException("You are not allowed to attach this book.");
    }

    private Concept getOwnedConcept(User user, Long conceptId) {
        return conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Concept not found."));
    }

    private boolean canReadThread(User viewer, ForumThread thread) {
        if (Objects.equals(thread.getAuthor().getId(), viewer.getId())) {
            return true;
        }
        return thread.getVisibility() == Visibility.PUBLIC || thread.getVisibility() == Visibility.SHARED;
    }

    private boolean canReadBook(User viewer, Book book) {
        if (book.getVisibility() == Visibility.PUBLIC) {
            return true;
        }
        if (book.getOwner() != null && Objects.equals(book.getOwner().getId(), viewer.getId())) {
            return true;
        }
        return userBookRepository.findByUserIdAndBookId(viewer.getId(), book.getId()).isPresent();
    }

    private void requireCanEdit(User user, ForumThread thread) {
        if (!canEdit(user, thread)) {
            throw new AccessDeniedException("You are not allowed to edit this forum thread.");
        }
    }

    private void requireCanEdit(User user, ForumComment comment) {
        if (!Objects.equals(comment.getAuthor().getId(), user.getId()) && !isModerator(user)) {
            throw new AccessDeniedException("You are not allowed to edit this forum comment.");
        }
    }

    private boolean canEdit(User user, ForumThread thread) {
        return Objects.equals(thread.getAuthor().getId(), user.getId()) || isModerator(user);
    }

    private boolean isModerator(User user) {
        if (user.getRole() == null) {
            return false;
        }
        RoleName role = user.getRole().getName();
        return role == RoleName.ADMIN || role == RoleName.MODERATOR;
    }

    private ForumCategoryResponse toCategoryResponse(ForumCategory category) {
        return new ForumCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder(),
                threadRepository.countByCategoryIdAndStatusNot(category.getId(), ForumThreadStatus.ARCHIVED),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    private ForumThreadResponse toThreadResponse(User viewer, ForumThread thread) {
        SourceReferenceResponse sourceReference = null;
        if (thread.getSourceReferenceId() != null) {
            sourceReference = sourceReferenceRepository.findByIdAndUserId(thread.getSourceReferenceId(), viewer.getId())
                    .map(sourceReferenceService::toResponse)
                    .orElse(null);
        }
        Book visibleBook = thread.getRelatedBook() != null && canReadBook(viewer, thread.getRelatedBook())
                ? thread.getRelatedBook()
                : null;
        Concept visibleConcept = thread.getRelatedConcept() != null
                && Objects.equals(thread.getRelatedConcept().getUser().getId(), viewer.getId())
                ? thread.getRelatedConcept()
                : null;

        return new ForumThreadResponse(
                thread.getId(),
                thread.getCategory().getId(),
                thread.getCategory().getName(),
                thread.getCategory().getSlug(),
                thread.getAuthor().getId(),
                displayName(thread.getAuthor()),
                thread.getTitle(),
                thread.getBodyMarkdown(),
                thread.getRelatedEntityType(),
                thread.getRelatedEntityId(),
                visibleBook == null ? null : visibleBook.getId(),
                visibleBook == null ? null : visibleBook.getTitle(),
                visibleConcept == null ? null : visibleConcept.getId(),
                visibleConcept == null ? null : visibleConcept.getName(),
                sourceReference == null ? null : thread.getSourceReferenceId(),
                sourceReference,
                thread.getStatus(),
                thread.getVisibility(),
                commentRepository.countByThreadIdAndArchivedFalse(thread.getId()),
                likeRepository.countByThreadId(thread.getId()),
                bookmarkRepository.countByThreadId(thread.getId()),
                likeRepository.existsByThreadIdAndUserId(thread.getId(), viewer.getId()),
                bookmarkRepository.existsByThreadIdAndUserId(thread.getId(), viewer.getId()),
                canEdit(viewer, thread),
                thread.getCreatedAt(),
                thread.getUpdatedAt());
    }

    private ForumCommentResponse toCommentResponse(User viewer, ForumComment comment) {
        return new ForumCommentResponse(
                comment.getId(),
                comment.getThread().getId(),
                comment.getAuthor().getId(),
                displayName(comment.getAuthor()),
                comment.getBodyMarkdown(),
                comment.getParentCommentId(),
                Objects.equals(comment.getAuthor().getId(), viewer.getId()) || isModerator(viewer),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

    private StructuredPostTemplateResponse toTemplateResponse(StructuredPostTemplate template) {
        return new StructuredPostTemplateResponse(
                template.getId(),
                template.getName(),
                template.getSlug(),
                template.getDescription(),
                template.getBodyMarkdownTemplate(),
                template.getDefaultRelatedEntityType(),
                template.getSortOrder());
    }

    private String displayName(User user) {
        if (user.getProfile() != null && StringUtils.hasText(user.getProfile().getDisplayName())) {
            return user.getProfile().getDisplayName();
        }
        return user.getEmail();
    }

    private String sanitizeMarkdown(String value) {
        return value == null ? "" : value.trim().replaceAll("<[^>]*>", "");
    }

    private String normalizeEntityType(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record DefaultCategory(String name, String description) {}
}
