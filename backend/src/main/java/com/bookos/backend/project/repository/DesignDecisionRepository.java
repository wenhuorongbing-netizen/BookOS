package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.DesignDecision;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignDecisionRepository extends JpaRepository<DesignDecision, Long> {

    long countByProjectOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<DesignDecision> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<DesignDecision> findByProjectOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    Optional<DesignDecision> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
