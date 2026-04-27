package com.bookos.backend.forum.repository;

import com.bookos.backend.forum.entity.ForumLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {

    Optional<ForumLike> findByThreadIdAndUserId(Long threadId, Long userId);

    boolean existsByThreadIdAndUserId(Long threadId, Long userId);

    long countByThreadId(Long threadId);
}
