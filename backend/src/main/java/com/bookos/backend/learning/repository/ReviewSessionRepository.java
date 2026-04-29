package com.bookos.backend.learning.repository;

import com.bookos.backend.learning.entity.ReviewSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewSessionRepository extends JpaRepository<ReviewSession, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<ReviewSession> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<ReviewSession> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedAtIsNotNull(Long userId);
}
