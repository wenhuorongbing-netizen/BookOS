package com.bookos.backend.knowledge.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "knowledge_objects",
        indexes = {
            @Index(name = "idx_knowledge_user_type", columnList = "user_id, type"),
            @Index(name = "idx_knowledge_user_book", columnList = "user_id, book_id"),
            @Index(name = "idx_knowledge_user_concept", columnList = "user_id, concept_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_knowledge_user_type_slug",
                columnNames = {"user_id", "type", "slug"}))
public class KnowledgeObject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private KnowledgeObjectType type;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, length = 260)
    private String slug;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(length = 80)
    private String ontologyLayer;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private SourceConfidence sourceConfidence;

    @Column(nullable = false, length = 64)
    private String createdBy = "USER";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private BookNote note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id")
    private Concept concept;

    private Long sourceReferenceId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tagsJson;

    @Column(nullable = false)
    private boolean archived = false;
}
