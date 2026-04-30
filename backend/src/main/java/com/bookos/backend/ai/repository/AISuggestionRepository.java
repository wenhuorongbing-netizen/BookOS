package com.bookos.backend.ai.repository;

import com.bookos.backend.ai.entity.AISuggestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AISuggestionRepository extends JpaRepository<AISuggestion, Long> {

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"book", "interaction"})
    List<AISuggestion> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "interaction"})
    Optional<AISuggestion> findByIdAndUserId(Long id, Long userId);
}
