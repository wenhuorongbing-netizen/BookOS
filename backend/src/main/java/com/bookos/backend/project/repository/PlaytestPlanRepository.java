package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.PlaytestPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaytestPlanRepository extends JpaRepository<PlaytestPlan, Long> {

    @EntityGraph(attributePaths = "project")
    List<PlaytestPlan> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = "project")
    Optional<PlaytestPlan> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
