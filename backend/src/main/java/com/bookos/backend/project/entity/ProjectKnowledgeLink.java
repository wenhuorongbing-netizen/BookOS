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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "project_knowledge_links",
        indexes = @Index(name = "idx_project_knowledge_links_project", columnList = "project_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_knowledge_link",
                columnNames = {"project_id", "target_type", "target_id", "relationship_type"}))
public class ProjectKnowledgeLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @Column(nullable = false, length = 64)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 64)
    private String relationshipType = "APPLIES_TO";

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_reference_id")
    private SourceReference sourceReference;
}
