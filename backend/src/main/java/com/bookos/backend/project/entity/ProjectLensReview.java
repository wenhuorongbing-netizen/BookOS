package com.bookos.backend.project.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
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
@Table(name = "project_lens_reviews", indexes = @Index(name = "idx_project_lens_reviews_project_status", columnList = "project_id, status"))
public class ProjectLensReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_object_id")
    private KnowledgeObject knowledgeObject;

    @Column(nullable = false, length = 500)
    private String question;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    private Integer score;

    @Column(nullable = false, length = 64)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reference_id")
    private SourceReference sourceReference;
}
