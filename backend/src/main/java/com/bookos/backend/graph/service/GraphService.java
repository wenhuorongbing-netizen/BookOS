package com.bookos.backend.graph.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.capture.repository.RawCaptureRepository;
import com.bookos.backend.daily.entity.DailyDesignPrompt;
import com.bookos.backend.daily.repository.DailyDesignPromptRepository;
import com.bookos.backend.forum.entity.ForumThread;
import com.bookos.backend.forum.repository.ForumThreadRepository;
import com.bookos.backend.graph.dto.GraphEdgeResponse;
import com.bookos.backend.graph.dto.GraphNodeResponse;
import com.bookos.backend.graph.dto.GraphResponse;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.note.repository.NoteBlockRepository;
import com.bookos.backend.project.entity.DesignDecision;
import com.bookos.backend.project.entity.GameProject;
import com.bookos.backend.project.entity.PlaytestFinding;
import com.bookos.backend.project.entity.ProjectApplication;
import com.bookos.backend.project.entity.ProjectKnowledgeLink;
import com.bookos.backend.project.entity.ProjectLensReview;
import com.bookos.backend.project.entity.ProjectProblem;
import com.bookos.backend.project.repository.DesignDecisionRepository;
import com.bookos.backend.project.repository.GameProjectRepository;
import com.bookos.backend.project.repository.PlaytestFindingRepository;
import com.bookos.backend.project.repository.ProjectApplicationRepository;
import com.bookos.backend.project.repository.ProjectKnowledgeLinkRepository;
import com.bookos.backend.project.repository.ProjectLensReviewRepository;
import com.bookos.backend.project.repository.ProjectProblemRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final BookService bookService;
    private final UserService userService;
    private final UserBookRepository userBookRepository;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final BookNoteRepository bookNoteRepository;
    private final NoteBlockRepository noteBlockRepository;
    private final RawCaptureRepository rawCaptureRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;
    private final GameProjectRepository projectRepository;
    private final ProjectProblemRepository projectProblemRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DesignDecisionRepository designDecisionRepository;
    private final PlaytestFindingRepository playtestFindingRepository;
    private final ProjectLensReviewRepository projectLensReviewRepository;
    private final ProjectKnowledgeLinkRepository projectKnowledgeLinkRepository;

    @Transactional(readOnly = true)
    public GraphResponse getWorkspaceGraph(
            String email,
            Long bookId,
            Long conceptId,
            Long projectId,
            String entityType,
            String relationshipType,
            SourceConfidence sourceConfidence,
            Instant createdFrom,
            Instant createdTo,
            Integer depth,
            Integer limit) {
        User user = userService.getByEmailRequired(email);
        GraphBuilder builder = new GraphBuilder();
        GraphFilter filter = new GraphFilter(entityType, relationshipType, sourceConfidence, createdFrom, createdTo, depth, limit);
        Set<String> roots = new LinkedHashSet<>();

        if (projectId != null) {
            GameProject project = projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(projectId, user.getId())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Project not found."));
            addProjectGraph(builder, user.getId(), project);
            roots.add(nodeId("PROJECT", project.getId()));
            return builder.toResponse(filter, roots);
        }

        if (conceptId != null) {
            Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Concept not found."));
            addConceptNode(builder, concept);
            addConceptSource(builder, user.getId(), nodeId("CONCEPT", concept.getId()), concept);
            roots.add(nodeId("CONCEPT", concept.getId()));
        }

        if (bookId != null) {
            Book book = bookService.getAccessibleBookEntity(email, bookId);
            addBookNode(builder, book);
            roots.add(nodeId("BOOK", book.getId()));
        } else {
            userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .map(UserBook::getBook)
                    .forEach(book -> addBookNode(builder, book));
        }

        addSourceReferences(builder, user.getId(), bookId);
        addNotes(builder, user.getId(), bookId);
        addNoteBlocks(builder, user.getId(), bookId);
        addCaptures(builder, user.getId(), bookId);
        addQuotes(builder, user.getId(), bookId);
        addActionItems(builder, user.getId(), bookId);
        addConcepts(builder, user.getId(), bookId);
        addKnowledgeObjects(builder, user.getId(), bookId);
        addDailyPrompts(builder, user.getId(), bookId);
        addForumThreads(builder, user, bookId);
        addProjects(builder, user.getId(), bookId);

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).forEach(link -> addEntityLink(builder, link));

        return builder.toResponse(filter, roots);
    }

    @Transactional(readOnly = true)
    public GraphResponse getBookGraph(
            String email,
            Long bookId,
            String entityType,
            String relationshipType,
            SourceConfidence sourceConfidence,
            Instant createdFrom,
            Instant createdTo,
            Integer depth,
            Integer limit) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, bookId);
        GraphBuilder builder = new GraphBuilder();
        addBookNode(builder, book);
        GraphFilter filter = new GraphFilter(entityType, relationshipType, sourceConfidence, createdFrom, createdTo, depth, limit);

        addSourceReferences(builder, user.getId(), bookId);
        addNotes(builder, user.getId(), bookId);
        addNoteBlocks(builder, user.getId(), bookId);
        addCaptures(builder, user.getId(), bookId);
        addQuotes(builder, user.getId(), bookId);
        addActionItems(builder, user.getId(), bookId);
        addConcepts(builder, user.getId(), bookId);
        addKnowledgeObjects(builder, user.getId(), bookId);
        addDailyPrompts(builder, user.getId(), bookId);
        addForumThreads(builder, user, bookId);
        addProjects(builder, user.getId(), bookId);

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(link -> touchesGraph(link, builder.nodes))
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse(filter, Set.of(nodeId("BOOK", bookId)));
    }

    @Transactional(readOnly = true)
    public GraphResponse getProjectGraph(
            String email,
            Long projectId,
            String entityType,
            String relationshipType,
            SourceConfidence sourceConfidence,
            Instant createdFrom,
            Instant createdTo,
            Integer depth,
            Integer limit) {
        User user = userService.getByEmailRequired(email);
        GameProject project = projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(projectId, user.getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Project not found."));
        GraphBuilder builder = new GraphBuilder();
        GraphFilter filter = new GraphFilter(entityType, relationshipType, sourceConfidence, createdFrom, createdTo, depth, limit);
        addProjectGraph(builder, user.getId(), project);

        forumThreadRepository.findByAuthorIdAndStatusNotOrderByUpdatedAtDesc(user.getId(), ForumThreadStatus.ARCHIVED).stream()
                .filter(thread -> thread.getRelatedProject() != null && Objects.equals(thread.getRelatedProject().getId(), projectId)
                        || "GAME_PROJECT".equals(thread.getRelatedEntityType()) && Objects.equals(thread.getRelatedEntityId(), projectId))
                .forEach(thread -> addForumThread(builder, user, thread));

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(link -> touchesGraph(link, builder.nodes))
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse(filter, Set.of(nodeId("PROJECT", projectId)));
    }

    @Transactional(readOnly = true)
    public GraphResponse getConceptGraph(
            String email,
            Long conceptId,
            String entityType,
            String relationshipType,
            SourceConfidence sourceConfidence,
            Instant createdFrom,
            Instant createdTo,
            Integer depth,
            Integer limit) {
        User user = userService.getByEmailRequired(email);
        Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Concept not found."));
        GraphBuilder builder = new GraphBuilder();
        GraphFilter filter = new GraphFilter(entityType, relationshipType, sourceConfidence, createdFrom, createdTo, depth, limit);
        addConceptNode(builder, concept);

        if (concept.getFirstBook() != null) {
            addBookNode(builder, concept.getFirstBook());
            builder.edge(nodeId("BOOK", concept.getFirstBook().getId()), nodeId("CONCEPT", concept.getId()), "MENTIONS");
        }
        addConceptSource(builder, user.getId(), nodeId("CONCEPT", concept.getId()), concept);

        knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(object -> object.getConcept() != null && object.getConcept().getId().equals(conceptId))
                .forEach(object -> addKnowledgeObject(builder, user.getId(), nodeId("CONCEPT", concept.getId()), object));

        forumThreadRepository.findByAuthorIdAndStatusNotOrderByUpdatedAtDesc(user.getId(), ForumThreadStatus.ARCHIVED).stream()
                .filter(thread -> thread.getRelatedConcept() != null && Objects.equals(thread.getRelatedConcept().getId(), conceptId))
                .forEach(thread -> addForumThread(builder, user, thread));

        entityLinkRepository.findByUserIdAndSourceTypeAndSourceId(user.getId(), "CONCEPT", conceptId)
                .forEach(link -> addEntityLink(builder, link));
        entityLinkRepository.findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(user.getId(), "CONCEPT", conceptId)
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse(filter, Set.of(nodeId("CONCEPT", conceptId)));
    }

    private void addSourceReferences(GraphBuilder builder, Long userId, Long bookId) {
        List<SourceReference> sources = bookId == null
                ? sourceReferenceRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : sourceReferenceRepository.findByBookIdAndUserIdOrderByCreatedAtDesc(bookId, userId);
        sources.forEach(source -> {
            if (source.getBook() != null) {
                addBookNode(builder, source.getBook());
                addSourceReference(builder, userId, nodeId("BOOK", source.getBook().getId()), source.getId());
            }
        });
    }

    private void addNotes(GraphBuilder builder, Long userId, Long bookId) {
        List<BookNote> notes = bookId == null
                ? bookNoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                : bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, userId);
        notes.forEach(note -> {
            String noteNodeId = nodeId("NOTE", note.getId());
            builder.node(noteNodeId, "NOTE", note.getTitle(), note.getId(), null, null, note.getCreatedAt());
            if (note.getBook() != null) {
                addBookNode(builder, note.getBook());
                builder.edge(nodeId("BOOK", note.getBook().getId()), noteNodeId, "SOURCE_OF");
            }
            sourceReferenceRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(note.getId(), userId).stream()
                    .findFirst()
                    .ifPresent(source -> addSourceReference(builder, userId, noteNodeId, source.getId()));
        });
    }

    private void addNoteBlocks(GraphBuilder builder, Long userId, Long bookId) {
        noteBlockRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(block -> bookId == null || block.getBook() != null && Objects.equals(block.getBook().getId(), bookId))
                .forEach(block -> {
                    String blockNodeId = nodeId("NOTE_BLOCK", block.getId());
                    builder.node(blockNodeId, "NOTE_BLOCK", truncate(block.getPlainText()), block.getId(), null, null, block.getCreatedAt());
                    if (block.getNote() != null) {
                        String noteNodeId = nodeId("NOTE", block.getNote().getId());
                        builder.node(noteNodeId, "NOTE", block.getNote().getTitle(), block.getNote().getId(), null, null, block.getNote().getCreatedAt());
                        builder.edge(noteNodeId, blockNodeId, "SOURCE_OF");
                    }
                    if (block.getBook() != null) {
                        addBookNode(builder, block.getBook());
                        builder.edge(nodeId("BOOK", block.getBook().getId()), blockNodeId, "SOURCE_OF");
                    }
                    sourceReferenceRepository.findByNoteBlockIdAndUserIdOrderByCreatedAtDesc(block.getId(), userId).stream()
                            .findFirst()
                            .ifPresent(source -> addSourceReference(builder, userId, blockNodeId, source.getId()));
                });
    }

    private void addCaptures(GraphBuilder builder, Long userId, Long bookId) {
        List<RawCapture> captures = bookId == null
                ? rawCaptureRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : rawCaptureRepository.findByUserIdAndBookIdOrderByCreatedAtDesc(userId, bookId);
        captures.forEach(capture -> {
            String captureNodeId = nodeId("RAW_CAPTURE", capture.getId());
            builder.node(captureNodeId, "RAW_CAPTURE", truncate(capture.getCleanText()), capture.getId(), null, null, capture.getCreatedAt());
            if (capture.getBook() != null) {
                addBookNode(builder, capture.getBook());
                builder.edge(nodeId("BOOK", capture.getBook().getId()), captureNodeId, "SOURCE_OF");
            }
            sourceReferenceRepository.findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(capture.getId(), userId).stream()
                    .findFirst()
                    .ifPresent(source -> addSourceReference(builder, userId, captureNodeId, source.getId()));
        });
    }

    private void addQuotes(GraphBuilder builder, Long userId, Long bookId) {
        List<Quote> quotes = bookId == null
                ? quoteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                : quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(userId, bookId);
        quotes.forEach(quote -> {
            String quoteNodeId = nodeId("QUOTE", quote.getId());
            builder.node(quoteNodeId, "QUOTE", truncate(quote.getText()), quote.getId());
            if (quote.getBook() != null) {
                addBookNode(builder, quote.getBook());
                builder.edge(nodeId("BOOK", quote.getBook().getId()), quoteNodeId, "SOURCE_OF");
            }
            addSourceReference(builder, userId, quoteNodeId, quote.getSourceReferenceId());
        });
    }

    private void addActionItems(GraphBuilder builder, Long userId, Long bookId) {
        List<ActionItem> actionItems = bookId == null
                ? actionItemRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                : actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(userId, bookId);
        actionItems.forEach(item -> {
            String actionNodeId = nodeId("ACTION_ITEM", item.getId());
            builder.node(actionNodeId, "ACTION_ITEM", item.getTitle(), item.getId());
            if (item.getBook() != null) {
                addBookNode(builder, item.getBook());
                builder.edge(nodeId("BOOK", item.getBook().getId()), actionNodeId, "SOURCE_OF");
            }
            addSourceReference(builder, userId, actionNodeId, item.getSourceReferenceId());
        });
    }

    private void addConcepts(GraphBuilder builder, Long userId, Long bookId) {
        List<Concept> concepts = bookId == null
                ? conceptRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId)
                : conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(userId, bookId);
        concepts.forEach(concept -> {
            addConceptNode(builder, concept);
            if (concept.getFirstBook() != null) {
                addBookNode(builder, concept.getFirstBook());
                builder.edge(nodeId("BOOK", concept.getFirstBook().getId()), nodeId("CONCEPT", concept.getId()), "MENTIONS");
            }
            addConceptSource(builder, userId, nodeId("CONCEPT", concept.getId()), concept);
        });
    }

    private void addKnowledgeObjects(GraphBuilder builder, Long userId, Long bookId) {
        knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId).stream()
                .filter(object -> bookId == null || object.getBook() != null && Objects.equals(object.getBook().getId(), bookId))
                .forEach(object -> {
                    String parentNodeId = object.getBook() == null ? null : nodeId("BOOK", object.getBook().getId());
                    if (object.getBook() != null) {
                        addBookNode(builder, object.getBook());
                    }
                    addKnowledgeObject(builder, userId, parentNodeId, object);
                });
    }

    private void addDailyPrompts(GraphBuilder builder, Long userId, Long bookId) {
        dailyDesignPromptRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(prompt -> bookId == null || prompt.getBook() != null && Objects.equals(prompt.getBook().getId(), bookId))
                .forEach(prompt -> addDailyPrompt(builder, userId, prompt));
    }

    private void addForumThreads(GraphBuilder builder, User user, Long bookId) {
        forumThreadRepository.findByAuthorIdAndStatusNotOrderByUpdatedAtDesc(user.getId(), ForumThreadStatus.ARCHIVED).stream()
                .filter(thread -> bookId == null || visibleForumBook(user, thread) != null
                        && Objects.equals(visibleForumBook(user, thread).getId(), bookId))
                .forEach(thread -> addForumThread(builder, user, thread));
    }

    private void addProjects(GraphBuilder builder, Long userId, Long bookId) {
        projectRepository.findByOwnerIdAndArchivedAtIsNullOrderByUpdatedAtDesc(userId)
                .forEach(project -> addProjectGraph(builder, userId, project, bookId));
    }

    private void addProjectGraph(GraphBuilder builder, Long userId, GameProject project) {
        addProjectGraph(builder, userId, project, null);
    }

    private void addProjectGraph(GraphBuilder builder, Long userId, GameProject project, Long bookId) {
        String projectNodeId = nodeId("PROJECT", project.getId());
        boolean projectAdded = false;

        for (ProjectProblem problem : projectProblemRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(problem.getRelatedSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String nodeId = nodeId("PROJECT_PROBLEM", problem.getId());
            builder.node(nodeId, "PROJECT_PROBLEM", problem.getTitle(), problem.getId());
            builder.edge(nodeId, projectNodeId, "EVIDENCE_FOR");
            addSourceReference(builder, userId, nodeId, sourceReferenceId(problem.getRelatedSourceReference()));
        }

        for (ProjectApplication application : projectApplicationRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(application.getSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String nodeId = nodeId("PROJECT_APPLICATION", application.getId());
            builder.node(nodeId, "PROJECT_APPLICATION", application.getTitle(), application.getId());
            builder.edge(nodeId, projectNodeId, "APPLIES_TO");
            addSourceReference(builder, userId, nodeId, sourceReferenceId(application.getSourceReference()));
            addProjectTargetEdge(builder, nodeId, application.getSourceEntityType(), application.getSourceEntityId(), "RELATED_TO");
        }

        for (DesignDecision decision : designDecisionRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(decision.getSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String nodeId = nodeId("DESIGN_DECISION", decision.getId());
            builder.node(nodeId, "DESIGN_DECISION", decision.getTitle(), decision.getId());
            builder.edge(nodeId, projectNodeId, "DECISION_FOR");
            addSourceReference(builder, userId, nodeId, sourceReferenceId(decision.getSourceReference()));
        }

        for (PlaytestFinding finding : playtestFindingRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(finding.getSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String nodeId = nodeId("PLAYTEST_FINDING", finding.getId());
            builder.node(nodeId, "PLAYTEST_FINDING", finding.getTitle(), finding.getId());
            builder.edge(nodeId, projectNodeId, "FINDING_FOR");
            addSourceReference(builder, userId, nodeId, sourceReferenceId(finding.getSourceReference()));
        }

        for (ProjectLensReview review : projectLensReviewRepository.findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(review.getSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String nodeId = nodeId("PROJECT_LENS_REVIEW", review.getId());
            builder.node(nodeId, "PROJECT_LENS_REVIEW", truncate(review.getQuestion()), review.getId());
            builder.edge(nodeId, projectNodeId, "REVIEWS_WITH_LENS");
            addSourceReference(builder, userId, nodeId, sourceReferenceId(review.getSourceReference()));
            if (review.getKnowledgeObject() != null) {
                addKnowledgeObject(builder, userId, null, review.getKnowledgeObject());
                builder.edge(nodeId, nodeId("KNOWLEDGE_OBJECT", review.getKnowledgeObject().getId()), "REVIEWS_WITH_LENS");
            }
        }

        for (ProjectKnowledgeLink link : projectKnowledgeLinkRepository.findByProjectIdAndProjectOwnerIdOrderByCreatedAtDesc(project.getId(), userId)) {
            if (bookId != null && !sourceBelongsToBook(link.getSourceReference(), bookId)) {
                continue;
            }
            addProjectNode(builder, project);
            projectAdded = true;
            String targetNodeId = nodeId(link.getTargetType(), link.getTargetId());
            builder.node(targetNodeId, normalizeType(link.getTargetType()), targetNodeId, link.getTargetId());
            builder.edge(projectNodeId, targetNodeId, link.getRelationshipType());
            addSourceReference(builder, userId, projectNodeId, sourceReferenceId(link.getSourceReference()));
        }

        if (bookId == null && !projectAdded) {
            addProjectNode(builder, project);
        }
    }

    private void addProjectNode(GraphBuilder builder, GameProject project) {
        builder.node(nodeId("PROJECT", project.getId()), "PROJECT", project.getTitle(), project.getId());
    }

    private void addProjectTargetEdge(GraphBuilder builder, String sourceNodeId, String targetType, Long targetId, String relationshipType) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            return;
        }
        String targetNodeId = nodeId(targetType, targetId);
        builder.node(targetNodeId, normalizeType(targetType), targetNodeId, targetId);
        builder.edge(sourceNodeId, targetNodeId, relationshipType);
    }

    private void addBookNode(GraphBuilder builder, Book book) {
        builder.node(nodeId("BOOK", book.getId()), "BOOK", book.getTitle(), book.getId(), null, null, book.getCreatedAt());
    }

    private void addConceptNode(GraphBuilder builder, Concept concept) {
        builder.node(
                nodeId("CONCEPT", concept.getId()),
                "CONCEPT",
                concept.getName(),
                concept.getId(),
                concept.getFirstSourceReference() == null ? null : concept.getFirstSourceReference().getId(),
                concept.getSourceConfidence(),
                concept.getCreatedAt());
    }

    private void addKnowledgeObject(GraphBuilder builder, Long userId, String parentNodeId, KnowledgeObject object) {
        String objectNodeId = nodeId("KNOWLEDGE_OBJECT", object.getId());
        builder.node(objectNodeId, "KNOWLEDGE_OBJECT", object.getTitle(), object.getId());
        if (parentNodeId != null) {
            builder.edge(parentNodeId, objectNodeId, "SOURCE_OF");
        }
        if (object.getConcept() != null) {
            addConceptNode(builder, object.getConcept());
            builder.edge(objectNodeId, nodeId("CONCEPT", object.getConcept().getId()), "RELATED_TO");
        }
        if (object.getNote() != null) {
            String noteNodeId = nodeId("NOTE", object.getNote().getId());
            builder.node(noteNodeId, "NOTE", object.getNote().getTitle(), object.getNote().getId());
            builder.edge(objectNodeId, noteNodeId, "DERIVED_FROM");
        }
        addSourceReference(builder, userId, objectNodeId, object.getSourceReferenceId());
    }

    private void addDailyPrompt(GraphBuilder builder, Long userId, DailyDesignPrompt prompt) {
        String promptNodeId = nodeId("DAILY_PROMPT", prompt.getId());
        builder.node(promptNodeId, "DAILY_PROMPT", truncate(prompt.getQuestion()), prompt.getId());
        if (prompt.getBook() != null) {
            addBookNode(builder, prompt.getBook());
            builder.edge(nodeId("BOOK", prompt.getBook().getId()), promptNodeId, "SOURCE_OF");
        }
        if (prompt.getKnowledgeObject() != null) {
            addKnowledgeObject(builder, userId, null, prompt.getKnowledgeObject());
            builder.edge(promptNodeId, nodeId("KNOWLEDGE_OBJECT", prompt.getKnowledgeObject().getId()), "APPLIES_TO");
        }
        addSourceReference(builder, userId, promptNodeId, prompt.getSourceReferenceId());
    }

    private void addForumThread(GraphBuilder builder, User user, ForumThread thread) {
        String threadNodeId = nodeId("FORUM_THREAD", thread.getId());
        builder.node(threadNodeId, "FORUM_THREAD", thread.getTitle(), thread.getId());

        Book visibleBook = visibleForumBook(user, thread);
        if (visibleBook != null) {
            addBookNode(builder, visibleBook);
            builder.edge(threadNodeId, nodeId("BOOK", visibleBook.getId()), "DISCUSSES");
        }
        if (thread.getRelatedConcept() != null
                && conceptRepository.findByIdAndUserIdAndArchivedFalse(thread.getRelatedConcept().getId(), user.getId()).isPresent()) {
            addConceptNode(builder, thread.getRelatedConcept());
            builder.edge(threadNodeId, nodeId("CONCEPT", thread.getRelatedConcept().getId()), "DISCUSSES");
        }
        if (thread.getRelatedProject() != null
                && projectRepository.findByIdAndOwnerIdAndArchivedAtIsNull(thread.getRelatedProject().getId(), user.getId()).isPresent()) {
            addProjectNode(builder, thread.getRelatedProject());
            builder.edge(threadNodeId, nodeId("PROJECT", thread.getRelatedProject().getId()), "DISCUSSES");
        }
        if (StringUtils.hasText(thread.getRelatedEntityType()) && thread.getRelatedEntityId() != null) {
            String relatedNodeId = nodeId(thread.getRelatedEntityType(), thread.getRelatedEntityId());
            builder.node(relatedNodeId, normalizeType(thread.getRelatedEntityType()), relatedNodeId, thread.getRelatedEntityId());
            builder.edge(threadNodeId, relatedNodeId, "DISCUSSES");
        }
        addSourceReference(builder, user.getId(), threadNodeId, thread.getSourceReferenceId());
    }

    private void addConceptSource(GraphBuilder builder, Long userId, String conceptNodeId, Concept concept) {
        if (concept.getFirstSourceReference() != null) {
            addSourceReference(builder, userId, conceptNodeId, concept.getFirstSourceReference().getId());
        }
    }

    private void addSourceReference(GraphBuilder builder, Long userId, String ownerNodeId, Long sourceReferenceId) {
        if (sourceReferenceId == null) {
            return;
        }
        sourceReferenceRepository.findByIdAndUserId(sourceReferenceId, userId).ifPresent(source -> {
            String sourceNodeId = nodeId("SOURCE_REFERENCE", source.getId());
            builder.node(
                    sourceNodeId,
                    "SOURCE_REFERENCE",
                    sourceLabel(source),
                    source.getId(),
                    source.getId(),
                    source.getSourceConfidence(),
                    source.getCreatedAt());
            builder.edge(
                    ownerNodeId,
                    sourceNodeId,
                    "DERIVED_FROM",
                    null,
                    source.getId(),
                    source.getSourceConfidence(),
                    "SYSTEM",
                    null,
                    source.getCreatedAt());
        });
    }

    private boolean touchesGraph(EntityLink link, Map<String, GraphNodeResponse> nodes) {
        return nodes.containsKey(nodeId(link.getSourceType(), link.getSourceId()))
                || nodes.containsKey(nodeId(link.getTargetType(), link.getTargetId()));
    }

    private void addEntityLink(GraphBuilder builder, EntityLink link) {
        String sourceNodeId = nodeId(link.getSourceType(), link.getSourceId());
        String targetNodeId = nodeId(link.getTargetType(), link.getTargetId());
        builder.node(sourceNodeId, normalizeType(link.getSourceType()), sourceNodeId, link.getSourceId());
        builder.node(targetNodeId, normalizeType(link.getTargetType()), targetNodeId, link.getTargetId());
        SourceReference sourceReference = link.getSourceReferenceId() == null
                ? null
                : sourceReferenceRepository.findByIdAndUserId(link.getSourceReferenceId(), link.getUser().getId()).orElse(null);
        builder.edge(
                sourceNodeId,
                targetNodeId,
                normalizeType(link.getRelationType()),
                link.getId(),
                link.getSourceReferenceId(),
                sourceReference == null ? null : sourceReference.getSourceConfidence(),
                link.getCreatedBy(),
                link.getNote(),
                link.getCreatedAt());
    }

    private Book visibleForumBook(User user, ForumThread thread) {
        Book book = thread.getRelatedBook();
        if (book == null) {
            return null;
        }
        return canReadBook(user, book) ? book : null;
    }

    private boolean canReadBook(User user, Book book) {
        return book.getVisibility() == Visibility.PUBLIC
                || book.getVisibility() == Visibility.SHARED
                || book.getOwner() != null && Objects.equals(book.getOwner().getId(), user.getId())
                || userBookRepository.findByUserIdAndBookId(user.getId(), book.getId()).isPresent();
    }

    private boolean sourceBelongsToBook(SourceReference sourceReference, Long bookId) {
        return sourceReference != null
                && sourceReference.getBook() != null
                && Objects.equals(sourceReference.getBook().getId(), bookId);
    }

    private Long sourceReferenceId(SourceReference sourceReference) {
        return sourceReference == null ? null : sourceReference.getId();
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

    private static String nodeId(String type, Long id) {
        return normalizeType(type).toLowerCase(Locale.ROOT) + ":" + id;
    }

    private static String normalizeType(String type) {
        return type == null ? "UNKNOWN" : type.trim().toUpperCase(Locale.ROOT);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "Untitled";
        }
        String clean = text.replaceAll("\\s+", " ").trim();
        return clean.length() > 64 ? clean.substring(0, 61) + "..." : clean;
    }

    private static final class GraphBuilder {
        private final Map<String, GraphNodeResponse> nodes = new LinkedHashMap<>();
        private final Map<String, GraphEdgeResponse> edges = new LinkedHashMap<>();

        void node(String id, String type, String label, Long entityId) {
            node(id, type, label, entityId, null, null, null);
        }

        void node(
                String id,
                String type,
                String label,
                Long entityId,
                Long sourceReferenceId,
                SourceConfidence sourceConfidence,
                Instant createdAt) {
            GraphNodeResponse existing = nodes.get(id);
            String resolvedLabel = StringUtils.hasText(label) ? label : id;
            if (existing == null) {
                nodes.put(id, new GraphNodeResponse(id, normalizeType(type), resolvedLabel, entityId, sourceReferenceId, sourceConfidence, createdAt));
                return;
            }

            boolean betterLabel = Objects.equals(existing.label(), existing.id()) && !Objects.equals(resolvedLabel, id);
            Long resolvedSourceReferenceId = existing.sourceReferenceId() == null ? sourceReferenceId : existing.sourceReferenceId();
            SourceConfidence resolvedConfidence = existing.sourceConfidence() == null ? sourceConfidence : existing.sourceConfidence();
            Instant resolvedCreatedAt = existing.createdAt() == null ? createdAt : existing.createdAt();
            if (betterLabel || resolvedSourceReferenceId != null || resolvedConfidence != null || resolvedCreatedAt != null) {
                nodes.put(id, new GraphNodeResponse(
                        id,
                        normalizeType(type),
                        betterLabel ? resolvedLabel : existing.label(),
                        entityId,
                        resolvedSourceReferenceId,
                        resolvedConfidence,
                        resolvedCreatedAt));
            }
        }

        void edge(String source, String target, String type) {
            edge(source, target, type, null, null, null, "SYSTEM", null, null);
        }

        void edge(
                String source,
                String target,
                String type,
                Long entityLinkId,
                Long sourceReferenceId,
                SourceConfidence sourceConfidence,
                String createdBy,
                String note,
                Instant createdAt) {
            if (source == null || target == null || source.equals(target)) {
                return;
            }
            String normalizedType = normalizeType(type);
            String normalizedCreatedBy = StringUtils.hasText(createdBy) ? createdBy.trim().toUpperCase(Locale.ROOT) : "SYSTEM";
            edges.putIfAbsent(
                    source + "->" + target + ":" + normalizedType + ":" + (entityLinkId == null ? "system" : entityLinkId),
                    new GraphEdgeResponse(
                            source,
                            target,
                            normalizedType,
                            entityLinkId,
                            sourceReferenceId,
                            sourceConfidence,
                            normalizedCreatedBy,
                            !"USER".equals(normalizedCreatedBy),
                            note,
                            createdAt));
        }

        GraphResponse toResponse(GraphFilter filter, Collection<String> rootNodeIds) {
            GraphFilter activeFilter = filter == null ? GraphFilter.empty() : filter;
            List<GraphEdgeResponse> scopedEdges = edges.values().stream()
                    .filter(activeFilter::matchesEdge)
                    .toList();

            Set<String> includedNodeIds = new LinkedHashSet<>();
            if (activeFilter.wantedEntityType() != null) {
                nodes.values().stream()
                        .filter(activeFilter::matchesNode)
                        .map(GraphNodeResponse::id)
                        .forEach(includedNodeIds::add);
                scopedEdges.stream()
                        .filter(edge -> includedNodeIds.contains(edge.source()) || includedNodeIds.contains(edge.target()))
                        .forEach(edge -> {
                            includedNodeIds.add(edge.source());
                            includedNodeIds.add(edge.target());
                        });
            } else {
                scopedEdges.forEach(edge -> {
                    includedNodeIds.add(edge.source());
                    includedNodeIds.add(edge.target());
                });
                nodes.values().stream()
                        .filter(activeFilter::matchesNode)
                        .map(GraphNodeResponse::id)
                        .forEach(includedNodeIds::add);
            }

            if (includedNodeIds.isEmpty() && activeFilter.isEmpty()) {
                includedNodeIds.addAll(nodes.keySet());
            }

            Integer depth = activeFilter.normalizedDepth();
            if (depth != null && rootNodeIds != null && !rootNodeIds.isEmpty()) {
                includedNodeIds.retainAll(depthLimitedNodeIds(rootNodeIds, scopedEdges, depth));
            }

            List<GraphNodeResponse> filteredNodes = nodes.values().stream()
                    .filter(node -> includedNodeIds.contains(node.id()))
                    .limit(activeFilter.normalizedLimit())
                    .toList();
            Set<String> finalNodeIds = filteredNodes.stream()
                    .map(GraphNodeResponse::id)
                    .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
            List<GraphEdgeResponse> filteredEdges = scopedEdges.stream()
                    .filter(edge -> finalNodeIds.contains(edge.source()) && finalNodeIds.contains(edge.target()))
                    .limit(activeFilter.normalizedLimit() * 2L)
                    .toList();
            return new GraphResponse(filteredNodes, filteredEdges);
        }

        private Set<String> depthLimitedNodeIds(Collection<String> roots, List<GraphEdgeResponse> scopedEdges, int depth) {
            Set<String> visited = new LinkedHashSet<>();
            ArrayDeque<NodeDepth> queue = new ArrayDeque<>();
            roots.stream().filter(nodes::containsKey).forEach(root -> {
                visited.add(root);
                queue.add(new NodeDepth(root, 0));
            });

            while (!queue.isEmpty()) {
                NodeDepth current = queue.removeFirst();
                if (current.depth >= depth) {
                    continue;
                }
                neighbors(current.nodeId, scopedEdges).stream()
                        .filter(visited::add)
                        .forEach(neighbor -> queue.add(new NodeDepth(neighbor, current.depth + 1)));
            }
            return visited;
        }

        private List<String> neighbors(String nodeId, List<GraphEdgeResponse> scopedEdges) {
            List<String> result = new ArrayList<>();
            for (GraphEdgeResponse edge : scopedEdges) {
                if (nodeId.equals(edge.source())) {
                    result.add(edge.target());
                }
                if (nodeId.equals(edge.target())) {
                    result.add(edge.source());
                }
            }
            return result;
        }
    }

    private record NodeDepth(String nodeId, int depth) {}

    private record GraphFilter(
            String entityType,
            String relationshipType,
            SourceConfidence sourceConfidence,
            Instant createdFrom,
            Instant createdTo,
            Integer depth,
            Integer limit) {

        static GraphFilter empty() {
            return new GraphFilter(null, null, null, null, null, null, null);
        }

        String wantedEntityType() {
            return StringUtils.hasText(entityType) ? normalizeType(entityType) : null;
        }

        String wantedRelationshipType() {
            return StringUtils.hasText(relationshipType) ? normalizeType(relationshipType) : null;
        }

        boolean matchesNode(GraphNodeResponse node) {
            return wantedEntityType() == null || wantedEntityType().equals(node.type());
        }

        boolean matchesEdge(GraphEdgeResponse edge) {
            if (wantedRelationshipType() != null && !wantedRelationshipType().equals(edge.type())) {
                return false;
            }
            if (sourceConfidence != null && edge.sourceConfidence() != sourceConfidence) {
                return false;
            }
            if (createdFrom != null && (edge.createdAt() == null || edge.createdAt().isBefore(createdFrom))) {
                return false;
            }
            return createdTo == null || edge.createdAt() != null && !edge.createdAt().isAfter(createdTo);
        }

        Integer normalizedDepth() {
            if (depth == null || depth < 1) {
                return null;
            }
            return Math.min(depth, 4);
        }

        long normalizedLimit() {
            if (limit == null || limit < 1) {
                return 160;
            }
            return Math.min(limit, 500);
        }

        boolean isEmpty() {
            return wantedEntityType() == null
                    && wantedRelationshipType() == null
                    && sourceConfidence == null
                    && createdFrom == null
                    && createdTo == null;
        }
    }
}
