package com.bookos.backend.graph.service;

import com.bookos.backend.action.repository.ActionItemRepository;
import com.bookos.backend.book.entity.Book;
import com.bookos.backend.book.service.BookService;
import com.bookos.backend.graph.dto.GraphEdgeResponse;
import com.bookos.backend.graph.dto.GraphNodeResponse;
import com.bookos.backend.graph.dto.GraphResponse;
import com.bookos.backend.knowledge.entity.Concept;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import com.bookos.backend.knowledge.repository.ConceptRepository;
import com.bookos.backend.knowledge.repository.KnowledgeObjectRepository;
import com.bookos.backend.link.entity.EntityLink;
import com.bookos.backend.link.repository.EntityLinkRepository;
import com.bookos.backend.note.repository.BookNoteRepository;
import com.bookos.backend.quote.repository.QuoteRepository;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.source.repository.SourceReferenceRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final BookService bookService;
    private final UserService userService;
    private final ConceptRepository conceptRepository;
    private final KnowledgeObjectRepository knowledgeObjectRepository;
    private final QuoteRepository quoteRepository;
    private final ActionItemRepository actionItemRepository;
    private final BookNoteRepository bookNoteRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final EntityLinkRepository entityLinkRepository;

    @Transactional(readOnly = true)
    public GraphResponse getBookGraph(String email, Long bookId) {
        User user = userService.getByEmailRequired(email);
        Book book = bookService.getAccessibleBookEntity(email, bookId);
        GraphBuilder builder = new GraphBuilder();
        String bookNodeId = nodeId("BOOK", book.getId());
        builder.node(bookNodeId, "BOOK", book.getTitle(), book.getId());

        conceptRepository.findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId)
                .forEach(concept -> {
                    String conceptNodeId = nodeId("CONCEPT", concept.getId());
                    builder.node(conceptNodeId, "CONCEPT", concept.getName(), concept.getId());
                    builder.edge(bookNodeId, conceptNodeId, "MENTIONS");
                    addConceptSource(builder, user.getId(), conceptNodeId, concept);
                });

        quoteRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId)
                .forEach(quote -> {
                    String quoteNodeId = nodeId("QUOTE", quote.getId());
                    builder.node(quoteNodeId, "QUOTE", truncate(quote.getText()), quote.getId());
                    builder.edge(bookNodeId, quoteNodeId, "HAS_QUOTE");
                    addSourceReference(builder, user.getId(), quoteNodeId, quote.getSourceReferenceId());
                });

        actionItemRepository.findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId(), bookId)
                .forEach(item -> {
                    String actionNodeId = nodeId("ACTION_ITEM", item.getId());
                    builder.node(actionNodeId, "ACTION_ITEM", item.getTitle(), item.getId());
                    builder.edge(bookNodeId, actionNodeId, "HAS_ACTION");
                    addSourceReference(builder, user.getId(), actionNodeId, item.getSourceReferenceId());
                });

        bookNoteRepository.findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(bookId, user.getId())
                .forEach(note -> {
                    String noteNodeId = nodeId("NOTE", note.getId());
                    builder.node(noteNodeId, "NOTE", note.getTitle(), note.getId());
                    builder.edge(bookNodeId, noteNodeId, "HAS_NOTE");
                });

        knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(object -> object.getBook() != null && object.getBook().getId().equals(bookId))
                .forEach(object -> addKnowledgeObject(builder, user.getId(), bookNodeId, object));

        entityLinkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(link -> touchesBookGraph(link, builder.nodes))
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse();
    }

    @Transactional(readOnly = true)
    public GraphResponse getConceptGraph(String email, Long conceptId) {
        User user = userService.getByEmailRequired(email);
        Concept concept = conceptRepository.findByIdAndUserIdAndArchivedFalse(conceptId, user.getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Concept not found."));
        GraphBuilder builder = new GraphBuilder();
        String conceptNodeId = nodeId("CONCEPT", concept.getId());
        builder.node(conceptNodeId, "CONCEPT", concept.getName(), concept.getId());

        if (concept.getFirstBook() != null) {
            String bookNodeId = nodeId("BOOK", concept.getFirstBook().getId());
            builder.node(bookNodeId, "BOOK", concept.getFirstBook().getTitle(), concept.getFirstBook().getId());
            builder.edge(bookNodeId, conceptNodeId, "MENTIONS");
        }
        addConceptSource(builder, user.getId(), conceptNodeId, concept);

        knowledgeObjectRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId()).stream()
                .filter(object -> object.getConcept() != null && object.getConcept().getId().equals(conceptId))
                .forEach(object -> addKnowledgeObject(builder, user.getId(), conceptNodeId, object));

        entityLinkRepository.findByUserIdAndSourceTypeAndSourceId(user.getId(), "CONCEPT", conceptId)
                .forEach(link -> addEntityLink(builder, link));
        entityLinkRepository.findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(user.getId(), "CONCEPT", conceptId)
                .forEach(link -> addEntityLink(builder, link));

        return builder.toResponse();
    }

    private void addKnowledgeObject(GraphBuilder builder, Long userId, String parentNodeId, KnowledgeObject object) {
        String nodeId = nodeId("KNOWLEDGE_OBJECT", object.getId());
        builder.node(nodeId, object.getType().name(), object.getTitle(), object.getId());
        builder.edge(parentNodeId, nodeId, "HAS_KNOWLEDGE");
        addSourceReference(builder, userId, nodeId, object.getSourceReferenceId());
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
            builder.edge(ownerNodeId, sourceNodeId, "HAS_SOURCE");
        });
    }

    private boolean touchesBookGraph(EntityLink link, Map<String, GraphNodeResponse> nodes) {
        return nodes.containsKey(nodeId(link.getSourceType(), link.getSourceId()))
                || nodes.containsKey(nodeId(link.getTargetType(), link.getTargetId()));
    }

    private void addEntityLink(GraphBuilder builder, EntityLink link) {
        String sourceNodeId = nodeId(link.getSourceType(), link.getSourceId());
        String targetNodeId = nodeId(link.getTargetType(), link.getTargetId());
        builder.node(sourceNodeId, normalizeType(link.getSourceType()), sourceNodeId, link.getSourceId());
        builder.node(targetNodeId, normalizeType(link.getTargetType()), targetNodeId, link.getTargetId());
        builder.edge(sourceNodeId, targetNodeId, link.getRelationType());
    }

    private String sourceLabel(SourceReference source) {
        if (source.getLocationLabel() != null && !source.getLocationLabel().isBlank()) {
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

    private String nodeId(String type, Long id) {
        return normalizeType(type).toLowerCase(java.util.Locale.ROOT) + ":" + id;
    }

    private String normalizeType(String type) {
        return type == null ? "UNKNOWN" : type.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String truncate(String text) {
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
            nodes.putIfAbsent(id, new GraphNodeResponse(id, type, label, entityId));
        }

        void edge(String source, String target, String type) {
            if (source.equals(target)) {
                return;
            }
            edges.putIfAbsent(source + "->" + target + ":" + type, new GraphEdgeResponse(source, target, type));
        }

        GraphResponse toResponse() {
            return new GraphResponse(List.copyOf(nodes.values()), List.copyOf(edges.values()));
        }
    }
}
