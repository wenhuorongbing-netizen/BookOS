package com.bookos.backend.learning.entity;

import com.bookos.backend.book.entity.Book;
import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "reading_sessions",
        indexes = {
            @Index(name = "idx_reading_sessions_user_started", columnList = "user_id, started_at"),
            @Index(name = "idx_reading_sessions_book_started", columnList = "book_id, started_at")
        })
public class ReadingSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    private Integer startPage;

    private Integer endPage;

    private Integer minutesRead;

    @Column(nullable = false)
    private Integer notesCount = 0;

    @Column(nullable = false)
    private Integer capturesCount = 0;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String reflection;
}
