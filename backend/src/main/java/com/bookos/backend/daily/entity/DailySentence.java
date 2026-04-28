package com.bookos.backend.daily.entity;

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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "daily_sentences",
        indexes = {
            @Index(name = "idx_daily_sentences_user_day_active", columnList = "user_id, daily_date, active"),
            @Index(name = "idx_daily_sentences_user_source", columnList = "user_id, source_type, source_id")
        })
public class DailySentence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "daily_date", nullable = false)
    private LocalDate day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(length = 64)
    private String sourceType;

    private Long sourceId;

    private Long sourceReferenceId;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String text;

    @Column(length = 220)
    private String attribution;

    private Integer pageStart;

    private Integer pageEnd;

    @Column(nullable = false)
    private boolean sourceBacked = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean skipped = false;
}
