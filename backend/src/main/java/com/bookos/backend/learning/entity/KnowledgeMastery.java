package com.bookos.backend.learning.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.source.entity.SourceReference;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "knowledge_mastery",
        indexes = {
            @Index(name = "idx_mastery_user_updated", columnList = "user_id, updated_at"),
            @Index(name = "idx_mastery_user_next", columnList = "user_id, next_review_at")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_mastery_user_target", columnNames = {"user_id", "target_type", "target_id"}))
public class KnowledgeMastery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private Integer familiarityScore = 0;

    @Column(nullable = false)
    private Integer usefulnessScore = 0;

    private Instant lastReviewedAt;

    private Instant nextReviewAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reference_id")
    private SourceReference sourceReference;
}
