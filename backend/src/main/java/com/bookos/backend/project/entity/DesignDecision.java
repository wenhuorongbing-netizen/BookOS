package com.bookos.backend.project.entity;

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
@Table(name = "design_decisions", indexes = @Index(name = "idx_design_decisions_project_status", columnList = "project_id, status"))
public class DesignDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String decision;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rationale;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tradeoffs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reference_id")
    private SourceReference sourceReference;

    @Column(nullable = false, length = 64)
    private String status = "PROPOSED";
}
