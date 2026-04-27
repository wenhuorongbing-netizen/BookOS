package com.bookos.backend.daily.repository;

import com.bookos.backend.daily.entity.DailyReflection;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReflectionRepository extends JpaRepository<DailyReflection, Long> {

    List<DailyReflection> findByUserIdAndDayOrderByCreatedAtDesc(Long userId, LocalDate day);

    List<DailyReflection> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);
}
