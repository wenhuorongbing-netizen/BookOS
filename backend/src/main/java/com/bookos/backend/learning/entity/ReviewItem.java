package com.bookos.backend.learning.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.source.entity.SourceReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "review_items",
        indexes = {
            @Index(name = "idx_review_items_session", columnList = "review_session_id"),
            @Index(name = "idx_review_items_target", columnList = "target_type, target_id")
        })
public class ReviewItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_session_id", nullable = false)
    private ReviewSession reviewSession;

    @Column(nullable = false, length = 64)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reference_id")
    private SourceReference sourceReference;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String prompt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String userResponse;

    @Column(nullable = false, length = 64)
    private String status = "OPEN";

    private Integer confidenceScore;
}
