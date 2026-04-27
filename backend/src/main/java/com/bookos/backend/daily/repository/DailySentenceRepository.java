package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailySentence;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySentenceRepository extends JpaRepository<DailySentence, Long> {

    @EntityGraph(attributePaths = {"book"})
    Optional<DailySentence> findFirstByUserIdAndDayAndActiveTrueOrderByUpdatedAtDesc(Long userId, LocalDate day);

    @EntityGraph(attributePaths = {"book"})
    Optional<DailySentence> findByIdAndUserId(Long id, Long userId);

    List<DailySentence> findByUserIdAndDayOrderByCreatedAtAsc(Long userId, LocalDate day);
}
