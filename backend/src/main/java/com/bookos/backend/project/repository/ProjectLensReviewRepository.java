package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.ProjectLensReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLensReviewRepository extends JpaRepository<ProjectLensReview, Long> {

    @EntityGraph(attributePaths = {"project", "knowledgeObject", "sourceReference"})
    List<ProjectLensReview> findByProjectIdAndProjectOwnerIdOrderByUpdatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "knowledgeObject", "sourceReference"})
    List<ProjectLensReview> findByProjectOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "knowledgeObject", "sourceReference"})
    Optional<ProjectLensReview> findByIdAndProjectOwnerId(Long id, Long ownerId);
}
