package com.bookos.backend.project.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.user.entity.User;
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
        name = "project_wizard_submissions",
        indexes = {
            @Index(name = "idx_project_wizard_submissions_owner", columnList = "owner_id"),
            @Index(name = "idx_project_wizard_submissions_project", columnList = "project_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_wizard_submission_idempotency",
                columnNames = {"project_id", "owner_id", "idempotency_key"}))
public class ProjectWizardSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private GameProject project;

    @Column(nullable = false, length = 120)
    private String idempotencyKey;

    @Column(length = 500)
    private String clientStepIntent;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String responseJson;
}
