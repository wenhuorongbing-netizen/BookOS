package com.bookos.backend.action.repository;

import com.bookos.backend.action.entity.ActionItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<ActionItem> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<ActionItem> findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId, Long bookId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    Optional<ActionItem> findByIdAndUserIdAndArchivedFalse(Long id, Long userId);
}
