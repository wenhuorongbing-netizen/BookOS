package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailyHistory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyHistoryRepository extends JpaRepository<DailyHistory, Long> {

    List<DailyHistory> findByUserIdAndDayAndItemTypeOrderByCreatedAtAsc(Long userId, LocalDate day, String itemType);

    List<DailyHistory> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}
