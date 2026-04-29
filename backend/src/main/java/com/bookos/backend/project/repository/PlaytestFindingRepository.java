package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.PlaytestFinding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaytestFindingRepository extends JpaRepository<PlaytestFinding, Long> {

    @EntityGraph(attributePaths = {"project", "session", "sourceReference"})
    List<PlaytestFinding> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "session", "sourceReference"})
    List<PlaytestFinding> findByProjectOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "session", "sourceReference"})
    Optional<PlaytestFinding> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
