package com.bookos.backend.knowledge.repository;

import com.bookos.backend.knowledge.entity.Concept;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptRepository extends JpaRepository<Concept, Long> {

    @EntityGraph(attributePaths = {"firstBook", "firstSourceReference"})
    List<Concept> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"firstBook", "firstSourceReference"})
    List<Concept> findByUserIdAndFirstBookIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId, Long firstBookId);

    @EntityGraph(attributePaths = {"firstBook", "firstSourceReference"})
    Optional<Concept> findByIdAndUserIdAndArchivedFalse(Long id, Long userId);

    @EntityGraph(attributePaths = {"firstBook", "firstSourceReference"})
    Optional<Concept> findByUserIdAndSlugAndArchivedFalse(Long userId, String slug);
}
