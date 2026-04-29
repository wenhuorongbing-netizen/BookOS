package com.bookos.backend.project.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.Visibility;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
        name = "game_projects",
        indexes = @Index(name = "idx_game_projects_owner_archived", columnList = "owner_id, archived_at"),
        uniqueConstraints = @UniqueConstraint(name = "uk_game_projects_owner_slug", columnNames = {"owner_id", "slug"}))
public class GameProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, length = 260)
    private String slug;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(length = 120)
    private String genre;

    @Column(length = 120)
    private String platform;

    @Column(nullable = false, length = 64)
    private String stage = "IDEATION";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    private Integer progressPercent = 0;

    private Instant archivedAt;
}
