package com.bookos.backend.knowledge.repository;

import com.bookos.backend.common.enums.KnowledgeObjectType;
import com.bookos.backend.knowledge.entity.KnowledgeObject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeObjectRepository extends JpaRepository<KnowledgeObject, Long> {

    long countByUserIdAndArchivedFalse(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "concept"})
    List<KnowledgeObject> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "concept"})
    Optional<KnowledgeObject> findByIdAndUserIdAndArchivedFalse(Long id, Long userId);

    @EntityGraph(attributePaths = {"book", "note", "concept"})
    Optional<KnowledgeObject> findByUserIdAndTypeAndSlugAndArchivedFalse(Long userId, KnowledgeObjectType type, String slug);
}
