package com.bookos.backend.quote.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "quotes",
        indexes = {
            @Index(name = "idx_quotes_user_book", columnList = "user_id, book_id"),
            @Index(name = "idx_quotes_user_source", columnList = "user_id, source_reference_id")
        })
public class Quote extends BaseEntity {

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

    @Lob
    @Column(nullable = false)
    private String text;

    @Column(length = 220)
    private String attribution;

    private Integer pageStart;

    private Integer pageEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    private boolean archived = false;
}
