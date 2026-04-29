package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.ProjectKnowledgeLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectKnowledgeLinkRepository extends JpaRepository<ProjectKnowledgeLink, Long> {

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<ProjectKnowledgeLink> findByProjectIdAndProjectOwnerIdOrderByCreatedAtDesc(Long projectId, Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    List<ProjectKnowledgeLink> findByProjectOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    Optional<ProjectKnowledgeLink> findByIdAndProjectOwnerId(Long id, Long ownerId);

    @EntityGraph(attributePaths = {"project", "sourceReference"})
    Optional<ProjectKnowledgeLink> findByProjectIdAndTargetTypeAndTargetIdAndRelationshipType(
            Long projectId,
            String targetType,
            Long targetId,
            String relationshipType);
}
