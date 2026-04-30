package com.bookos.backend.user.entity;

import com.bookos.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 120)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    private boolean onboardingCompleted = false;

    @Column(length = 80)
    private String primaryUseCase;

    @Column(length = 40)
    private String startingMode;

    @Column(length = 40)
    private String preferredDashboardMode;
}
