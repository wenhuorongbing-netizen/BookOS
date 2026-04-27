package com.bookos.backend.note.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "note_blocks")
public class NoteBlock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private BookNote note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private NoteBlockType blockType = NoteBlockType.NOTE;

    @Lob
    @Column(nullable = false)
    private String rawText;

    @Lob
    @Column(nullable = false)
    private String markdown;

    @Column(length = 5000)
    private String plainText;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    private Integer pageStart;

    private Integer pageEnd;

    @Column(length = 1000)
    private String parserWarnings;

    @OneToMany(mappedBy = "noteBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<SourceReference> sourceReferences = new ArrayList<>();
}
