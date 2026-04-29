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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playtest_plans", indexes = @Index(name = "idx_playtest_plans_project_status", columnList = "project_id, status"))
public class PlaytestPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String hypothesis;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String targetPlayers;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tasks;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String successCriteria;

    @Column(nullable = false, length = 64)
    private String status = "DRAFT";
}
