package com.bookos.backend.forum.repository;

import com.bookos.backend.common.enums.ForumReportStatus;
import com.bookos.backend.forum.entity.ForumReport;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {

    @EntityGraph(attributePaths = {"thread", "thread.category", "thread.author", "reporter", "reporter.profile"})
    List<ForumReport> findByStatusOrderByCreatedAtDesc(ForumReportStatus status);

    @EntityGraph(attributePaths = {"thread", "thread.category", "thread.author", "reporter", "reporter.profile"})
    List<ForumReport> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"thread", "thread.category", "thread.author", "reporter", "reporter.profile"})
    Optional<ForumReport> findById(Long id);

    long countByThreadId(Long threadId);

    long countByThreadIdAndStatus(Long threadId, ForumReportStatus status);
}
