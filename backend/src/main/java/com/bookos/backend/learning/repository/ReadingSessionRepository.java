package com.bookos.backend.learning.repository;

import com.bookos.backend.learning.entity.ReadingSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    @EntityGraph(attributePaths = {"book"})
    List<ReadingSession> findByUserIdOrderByStartedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book"})
    List<ReadingSession> findByUserIdAndBookIdOrderByStartedAtDesc(Long userId, Long bookId);

    @EntityGraph(attributePaths = {"book"})
    Optional<ReadingSession> findByIdAndUserId(Long id, Long userId);
}
