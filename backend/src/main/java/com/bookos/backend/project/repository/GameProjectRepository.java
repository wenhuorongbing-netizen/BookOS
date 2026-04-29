package com.bookos.backend.project.repository;

import com.bookos.backend.project.entity.GameProject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameProjectRepository extends JpaRepository<GameProject, Long> {

    List<GameProject> findByOwnerIdAndArchivedAtIsNullOrderByUpdatedAtDesc(Long ownerId);

    Optional<GameProject> findByIdAndOwnerIdAndArchivedAtIsNull(Long id, Long ownerId);

    boolean existsByOwnerIdAndSlug(Long ownerId, String slug);
}
