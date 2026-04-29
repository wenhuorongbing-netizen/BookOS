package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.PlaytestSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaytestSessionRepository extends JpaRepository<PlaytestSession, Long> {

    @EntityGraph(attributePaths = {"project", "plan"})
    Optional<PlaytestSession> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
