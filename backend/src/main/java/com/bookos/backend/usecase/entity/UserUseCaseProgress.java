package com.bookos.backend.usecase.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
        name = "user_use_case_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_use_case_progress_user_slug", columnNames = {"user_id", "use_case_slug"}),
        indexes = @Index(name = "idx_use_case_progress_user_status", columnList = "user_id, status"))
public class UserUseCaseProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "use_case_slug", nullable = false, length = 120)
    private String useCaseSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UseCaseProgressStatus status = UseCaseProgressStatus.NOT_STARTED;

    @Column(nullable = false)
    private Integer currentStep = 0;

    @Column(name = "completed_step_keys", columnDefinition = "TEXT")
    private String completedStepKeys;

    private Instant startedAt;

    private Instant completedAt;
}
