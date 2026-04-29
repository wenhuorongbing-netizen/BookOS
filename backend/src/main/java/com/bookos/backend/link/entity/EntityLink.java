package com.bookos.backend.link.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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
        name = "entity_links",
        indexes = {
            @Index(name = "idx_entity_links_source", columnList = "user_id, source_type, source_id"),
            @Index(name = "idx_entity_links_target", columnList = "user_id, target_type, target_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_entity_links_unique_relation",
                columnNames = {"user_id", "source_type", "source_id", "target_type", "target_id", "relation_type"}))
public class EntityLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "relation_type", nullable = false, length = 80)
    private String relationType;

    private Long sourceReferenceId;

    @Column(columnDefinition = "LONGTEXT")
    private String note;

    @Column(nullable = false, length = 32)
    private String createdBy = "SYSTEM";
}
