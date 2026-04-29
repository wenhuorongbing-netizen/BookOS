package com.bookos.backend.graph.service;

import com.bookos.backend.action.entity.ActionItem;
import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.book.repository.UserBookRepository;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
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
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.quote.entity.Quote;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
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
    private final SourceReferenceRepository sourceReferenceRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final DailyDesignPromptRepository dailyDesignPromptRepository;

    @Transactional(readOnly = true)
    public GraphResponse getWorkspaceGraph(String email, Long bookId, String entityType, String relationshipType) {
        User user = userService.getByEmailRequired(email);
        GraphBuilder builder = new GraphBuilder();

        if (bookId != null) {
            addBookNode(builder, bookService.getAccessibleBookEntity(email, bookId));
        } else {
            userBookRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                    .map(UserBook::getBook)
                    .forEach(book -> addBookNode(builder, book));
        }

        addSourceReferences(builder, user.getId(), bookId);
        addNotes(builder, user.getId(), bookId);
        addQuotes(builder, user.getId(), bookId);
        addActionItems(builder, user.getId(), bookId);
        addConcepts(builder, user.getId(), bookId);
        addKnowledgeObjects(builder, user.getId(), bookId);
        addDailyPrompts(builder, user.getId(), bookId);
        addForumThreads(builder, user, bookId);

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).forEach(link -> addEntityLink(builder, link));

        return builder.toResponse(entityType, relationshipType);
    }

    @Transactional(readOnly = true)
    public GraphResponse getBookGraph(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, bookId);
        GraphBuilder builder = new GraphBuilder();
        addBookNode(builder, book);

        addSourceReferences(builder, user.getId(), bookId);
        addNotes(builder, user.getId(), bookId);
        addQuotes(builder, user.getId(), bookId);
        addActionItems(builder, user.getId(), bookId);
        addConcepts(builder, user.getId(), bookId);
        addKnowledgeObjects(builder, user.getId(), bookId);
        addDailyPrompts(builder, user.getId(), bookId);
        addForumThreads(builder, user, bookId);

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(link -> touchesGraph(link, builder.nodes))
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse();
    }

    @Transactional(readOnly = true)
    public GraphResponse getConceptGraph(String email, Long conceptId) {
        User user = userService.getByEmailRequired(email);
        Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Concept not found."));
        GraphBuilder builder = new GraphBuilder();
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

        return builder.toResponse();
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
            builder.node(noteNodeId, "NOTE", note.getTitle(), note.getId());
            if (note.getBook() != null) {
                addBookNode(builder, note.getBook());
                builder.edge(nodeId("BOOK", note.getBook().getId()), noteNodeId, "SOURCE_OF");
            }
            sourceReferenceRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(note.getId(), userId).stream()
                    .findFirst()
                    .ifPresent(source -> addSourceReference(builder, userId, noteNodeId, source.getId()));
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

    private void addBookNode(GraphBuilder builder, Book book) {
        builder.node(nodeId("BOOK", book.getId()), "BOOK", book.getTitle(), book.getId());
    }

    private void addConceptNode(GraphBuilder builder, Concept concept) {
        builder.node(nodeId("CONCEPT", concept.getId()), "CONCEPT", concept.getName(), concept.getId());
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
            builder.node(sourceNodeId, "SOURCE_REFERENCE", sourceLabel(source), source.getId());
            builder.edge(ownerNodeId, sourceNodeId, "DERIVED_FROM");
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
        builder.edge(sourceNodeId, targetNodeId, normalizeType(link.getRelationType()));
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
            GraphNodeResponse existing = nodes.get(id);
            String resolvedLabel = StringUtils.hasText(label) ? label : id;
            if (existing == null || Objects.equals(existing.label(), existing.id())) {
                nodes.put(id, new GraphNodeResponse(id, normalizeType(type), resolvedLabel, entityId));
            }
        }

        void edge(String source, String target, String type) {
            if (source == null || target == null || source.equals(target)) {
                return;
            }
            String normalizedType = normalizeType(type);
            edges.putIfAbsent(source + "->" + target + ":" + normalizedType, new GraphEdgeResponse(source, target, normalizedType));
        }

        GraphResponse toResponse() {
            return new GraphResponse(List.copyOf(nodes.values()), List.copyOf(edges.values()));
        }

        GraphResponse toResponse(String entityType, String relationshipType) {
            String wantedEntityType = StringUtils.hasText(entityType) ? normalizeType(entityType) : null;
            String wantedRelationshipType = StringUtils.hasText(relationshipType) ? normalizeType(relationshipType) : null;
            List<GraphEdgeResponse> scopedEdges = edges.values().stream()
                    .filter(edge -> wantedRelationshipType == null || wantedRelationshipType.equals(edge.type()))
                    .toList();

            if (wantedEntityType == null && wantedRelationshipType == null) {
                return toResponse();
            }

            Set<String> includedNodeIds = new LinkedHashSet<>();
            if (wantedEntityType != null) {
                nodes.values().stream()
                        .filter(node -> wantedEntityType.equals(node.type()))
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
            }

            List<GraphNodeResponse> filteredNodes = nodes.values().stream()
                    .filter(node -> includedNodeIds.contains(node.id()))
                    .toList();
            List<GraphEdgeResponse> filteredEdges = scopedEdges.stream()
                    .filter(edge -> includedNodeIds.contains(edge.source()) && includedNodeIds.contains(edge.target()))
                    .toList();
            return new GraphResponse(filteredNodes, filteredEdges);
        }
    }
}
