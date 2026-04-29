package com.bookos.backend.project.entity;

import com.bookos.backend.common.BaseEntity;
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
@Table(name = "playtest_sessions", indexes = @Index(name = "idx_playtest_sessions_project", columnList = "project_id"))
public class PlaytestSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlaytestPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    private Instant sessionDate;

    @Column(length = 180)
    private String participantLabel;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String notes;
}
