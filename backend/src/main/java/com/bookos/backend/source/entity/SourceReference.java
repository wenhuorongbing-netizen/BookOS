package com.bookos.backend.source.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.SourceConfidence;
import com.bookos.backend.note.entity.BookNote;
import com.bookos.backend.note.entity.NoteBlock;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "source_references")
public class SourceReference extends BaseEntity {

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

    @Column(nullable = false, length = 64)
    private String sourceType;

    private Long chapterId;

    private Long rawCaptureId;

    private Integer pageStart;

    private Integer pageEnd;

    @Column(length = 255)
    private String locationLabel;

    @Lob
    private String sourceText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceConfidence sourceConfidence = SourceConfidence.LOW;
}
