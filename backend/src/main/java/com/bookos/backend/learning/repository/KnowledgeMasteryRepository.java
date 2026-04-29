package com.bookos.backend.learning.repository;

import com.bookos.backend.learning.entity.KnowledgeMastery;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeMasteryRepository extends JpaRepository<KnowledgeMastery, Long> {

    @EntityGraph(attributePaths = {"sourceReference", "sourceReference.book"})
    List<KnowledgeMastery> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"sourceReference", "sourceReference.book"})
    Optional<KnowledgeMastery> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    long countByUserId(Long userId);

    long countByUserIdAndNextReviewAtBefore(Long userId, Instant instant);
}
