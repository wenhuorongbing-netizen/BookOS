package com.bookos.backend.book.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.OwnershipStatus;
import com.bookos.backend.common.enums.ReadingFormat;
import com.bookos.backend.common.enums.ReadingStatus;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
        name = "user_books",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
public class UserBook extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReadingStatus readingStatus = ReadingStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReadingFormat readingFormat = ReadingFormat.PHYSICAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OwnershipStatus ownershipStatus = OwnershipStatus.OWNED;

    @Column(nullable = false)
    private Integer progressPercent = 0;

    @Column
    private Integer rating;
}
