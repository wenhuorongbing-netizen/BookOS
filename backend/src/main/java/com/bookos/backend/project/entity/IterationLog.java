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
@Table(name = "iteration_logs", indexes = @Index(name = "idx_iteration_logs_project", columnList = "project_id"))
public class IterationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @Column(nullable = false, length = 220)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String changesMade;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String evidence;
}
