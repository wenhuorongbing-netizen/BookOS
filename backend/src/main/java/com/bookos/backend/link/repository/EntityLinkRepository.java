package com.bookos.backend.link.repository;

import com.bookos.backend.link.entity.EntityLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntityLinkRepository extends JpaRepository<EntityLink, Long> {

    List<EntityLink> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<EntityLink> findByUserIdAndSourceTypeAndSourceIdOrderByCreatedAtDesc(Long userId, String sourceType, Long sourceId);

    List<EntityLink> findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(Long userId, String targetType, Long targetId);

    List<EntityLink> findByUserIdAndSourceTypeAndSourceId(Long userId, String sourceType, Long sourceId);

    Optional<EntityLink> findByUserIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetIdAndRelationType(
            Long userId,
            String sourceType,
            Long sourceId,
            String targetType,
            Long targetId,
            String relationType);

    Optional<EntityLink> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndTargetTypeAndTargetIdAndRelationType(Long userId, String targetType, Long targetId, String relationType);
}
