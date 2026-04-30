package com.bookos.backend.demo.entity;

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
        name = "demo_records",
        indexes = @Index(name = "idx_demo_records_user_type", columnList = "user_id, entity_type"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_demo_records_user_entity",
                columnNames = {"user_id", "entity_type", "entity_id"}))
public class DemoRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;
}
