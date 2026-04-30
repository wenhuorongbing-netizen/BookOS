package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.ProjectApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    long countByProjectOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<ProjectApplication> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<ProjectApplication> findByProjectOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    Optional<ProjectApplication> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
