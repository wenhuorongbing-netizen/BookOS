package com.bookos.backend.daily.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
        name = "daily_history",
        indexes = @Index(name = "idx_daily_history_user_day_type", columnList = "user_id, daily_date, item_type"))
public class DailyHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "daily_date", nullable = false)
    private LocalDate day;

    @Column(nullable = false, length = 32)
    private String itemType;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 64)
    private String sourceType;

    private Long sourceId;

    private Long dailySentenceId;

    private Long dailyDesignPromptId;
}
