package com.bookos.backend.forum.entity;

import com.bookos.backend.common.BaseEntity;
import com.bookos.backend.common.enums.ForumReportStatus;
import com.bookos.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "forum_reports")
public class ForumReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id", nullable = false)
    private ForumThread thread;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(length = 120)
    private String reason;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String details;

    @Column(nullable = false)
    private boolean resolved = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ForumReportStatus status = ForumReportStatus.OPEN;
}
