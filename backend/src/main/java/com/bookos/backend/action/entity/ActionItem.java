package com.bookos.backend.action.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.ActionPriority;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "action_items",
        indexes = {
            @Index(name = "idx_action_items_user_book", columnList = "user_id, book_id"),
            @Index(name = "idx_action_items_user_source", columnList = "user_id, source_reference_id")
        })
public class ActionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private BookNote note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_block_id")
    private NoteBlock noteBlock;

    private Long rawCaptureId;

    private Long sourceReferenceId;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ActionPriority priority = ActionPriority.MEDIUM;

    private Integer pageStart;

    private Integer pageEnd;

    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    private boolean archived = false;
}
