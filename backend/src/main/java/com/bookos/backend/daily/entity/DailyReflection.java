package com.bookos.backend.daily.entity;

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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "daily_reflections",
        indexes = @Index(name = "idx_daily_reflections_user_day", columnList = "user_id, daily_date"))
public class DailyReflection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "daily_date", nullable = false)
    private LocalDate day;

    @Column(nullable = false, length = 32)
    private String targetType;

    private Long dailySentenceId;

    private Long dailyDesignPromptId;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String reflectionText;
}
