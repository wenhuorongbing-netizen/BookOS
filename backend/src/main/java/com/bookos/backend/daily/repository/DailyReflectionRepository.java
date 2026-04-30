package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailyReflection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DailyReflectionRepository extends JpaRepository<DailyReflection, Long> {

    List<DailyReflection> findByUserIdAndDayOrderByCreatedAtDesc(Long userId, LocalDate day);

    List<DailyReflection> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DailyReflection reflection where reflection.user.id = :userId and reflection.dailySentenceId in :dailySentenceIds")
    void deleteByUserIdAndDailySentenceIds(Long userId, List<Long> dailySentenceIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DailyReflection reflection where reflection.user.id = :userId and reflection.dailyDesignPromptId in :dailyDesignPromptIds")
    void deleteByUserIdAndDailyDesignPromptIds(Long userId, List<Long> dailyDesignPromptIds);
}
