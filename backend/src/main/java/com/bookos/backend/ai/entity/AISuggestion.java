package com.bookos.backend.ai.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.AISuggestionStatus;
import com.bookos.backend.common.enums.AISuggestionType;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_suggestions")
public class AISuggestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interaction_id", nullable = false)
    private AIInteraction interaction;

    @Column(nullable = false, length = 80)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AISuggestionType suggestionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AISuggestionStatus status = AISuggestionStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private Long sourceReferenceId;

    @Lob
    @Column(nullable = false)
    private String draftText;

    @Lob
    @Column(nullable = false)
    private String draftJson;

    private Instant acceptedAt;

    private Instant rejectedAt;
}
