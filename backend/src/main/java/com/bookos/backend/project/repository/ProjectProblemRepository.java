package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.ProjectProblem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProblemRepository extends JpaRepository<ProjectProblem, Long> {

    @EntityGraph(attributePaths = {"project", "relatedSourceReference"})
    List<ProjectProblem> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "relatedSourceReference"})
    List<ProjectProblem> findByProjectOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "relatedSourceReference"})
    Optional<ProjectProblem> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
