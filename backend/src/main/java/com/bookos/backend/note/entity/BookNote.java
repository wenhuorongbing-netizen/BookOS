package com.bookos.backend.note.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.Visibility;
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
@Table(name = "book_notes")
public class BookNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(nullable = false)
    private String markdown;

    @Column(length = 1000)
    private String threeSentenceSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    private boolean archived = false;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<NoteBlock> blocks = new ArrayList<>();
}
