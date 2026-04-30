package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailyHistory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DailyHistoryRepository extends JpaRepository<DailyHistory, Long> {

    List<DailyHistory> findByUserIdAndDayAndItemTypeOrderByCreatedAtAsc(Long userId, LocalDate day, String itemType);

    List<DailyHistory> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DailyHistory history where history.user.id = :userId and history.dailySentenceId in :dailySentenceIds")
    void deleteByUserIdAndDailySentenceIds(Long userId, List<Long> dailySentenceIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DailyHistory history where history.user.id = :userId and history.dailyDesignPromptId in :dailyDesignPromptIds")
    void deleteByUserIdAndDailyDesignPromptIds(Long userId, List<Long> dailyDesignPromptIds);
}
