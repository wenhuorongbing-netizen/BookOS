package com.bookos.backend.forum.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.ForumThreadStatus;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.knowledge.entity.Concept;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "forum_threads",
        indexes = {
            @Index(name = "idx_forum_threads_category_status", columnList = "category_id, status"),
            @Index(name = "idx_forum_threads_author_status", columnList = "author_id, status"),
            @Index(name = "idx_forum_threads_related", columnList = "related_entity_type, related_entity_id")
        })
public class ForumThread extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ForumCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String bodyMarkdown;

    @Column(length = 64)
    private String relatedEntityType;

    private Long relatedEntityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_book_id")
    private Book relatedBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_concept_id")
    private Concept relatedConcept;

    private Long sourceReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ForumThreadStatus status = ForumThreadStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.SHARED;
}
