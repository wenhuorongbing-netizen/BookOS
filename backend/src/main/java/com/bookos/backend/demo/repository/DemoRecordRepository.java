package com.bookos.backend.demo.repository;

import com.bookos.backend.demo.entity.DemoRecord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoRecordRepository extends JpaRepository<DemoRecord, Long> {

    List<DemoRecord> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<DemoRecord> findByUserIdAndEntityType(Long userId, String entityType);

    List<DemoRecord> findByUserIdAndEntityTypeIn(Long userId, Collection<String> entityTypes);

    Optional<DemoRecord> findByUserIdAndEntityTypeAndEntityId(Long userId, String entityType, Long entityId);

    void deleteByUserId(Long userId);
}
