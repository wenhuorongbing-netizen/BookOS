package com.bookos.backend.learning.entity;

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
@Table(name = "review_sessions", indexes = @Index(name = "idx_review_sessions_user_started", columnList = "user_id, started_at"))
public class ReviewSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Column(nullable = false, length = 64)
    private String mode = "SOURCE_REVIEW";

    @Column(nullable = false, length = 64)
    private String scopeType = "GENERAL";

    private Long scopeId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;
}
