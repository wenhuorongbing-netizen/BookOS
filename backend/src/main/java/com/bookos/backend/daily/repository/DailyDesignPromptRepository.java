package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailyDesignPrompt;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyDesignPromptRepository extends JpaRepository<DailyDesignPrompt, Long> {

    @EntityGraph(attributePaths = {"book", "knowledgeObject"})
    Optional<DailyDesignPrompt> findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(Long userId, LocalDate day);

    @EntityGraph(attributePaths = {"book", "knowledgeObject"})
    Optional<DailyDesignPrompt> findByIdAndUserId(Long id, Long userId);

    List<DailyDesignPrompt> findByUserIdAndDayOrderByCreatedAtAsc(Long userId, LocalDate day);
}
