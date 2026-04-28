package com.bookos.backend.knowledge.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.source.entity.SourceReference;
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
        name = "concepts",
        indexes = {
            @Index(name = "idx_concepts_user_slug", columnList = "user_id, slug"),
            @Index(name = "idx_concepts_user_book", columnList = "user_id, first_book_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_concepts_user_slug", columnNames = {"user_id", "slug"}))
public class Concept extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 220)
    private String slug;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_book_id")
    private Book firstBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_source_reference_id")
    private SourceReference firstSourceReference;

    @Column(nullable = false)
    private Integer mentionCount = 0;

    @Column(nullable = false)
    private boolean archived = false;
}
