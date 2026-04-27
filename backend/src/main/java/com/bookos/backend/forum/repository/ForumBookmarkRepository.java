package com.bookos.backend.forum.repository;

import com.bookos.backend.forum.entity.ForumBookmark;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumBookmarkRepository extends JpaRepository<ForumBookmark, Long> {

    Optional<ForumBookmark> findByThreadIdAndUserId(Long threadId, Long userId);

    boolean existsByThreadIdAndUserId(Long threadId, Long userId);

    long countByThreadId(Long threadId);
}
