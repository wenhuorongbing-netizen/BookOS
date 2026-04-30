package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.ProjectWizardSubmission;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectWizardSubmissionRepository extends JpaRepository<ProjectWizardSubmission, Long> {

    @EntityGraph(attributePaths = {"owner", "project"})
    Optional<ProjectWizardSubmission> findByProjectIdAndOwnerIdAndIdempotencyKey(
            Long projectId,
            Long ownerId,
            String idempotencyKey);
}
