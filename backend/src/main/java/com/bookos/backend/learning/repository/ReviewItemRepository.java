package com.bookos.backend.learning.repository;

import com.bookos.backend.learning.entity.ReviewItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewItemRepository extends JpaRepository<ReviewItem, Long> {

    @EntityGraph(attributePaths = {"reviewSession", "sourceReference", "sourceReference.book"})
    List<ReviewItem> findByReviewSessionIdAndReviewSessionUserIdOrderByCreatedAtAsc(Long reviewSessionId, Long userId);

    @EntityGraph(attributePaths = {"reviewSession", "sourceReference", "sourceReference.book"})
    Optional<ReviewItem> findByIdAndReviewSessionUserId(Long id, Long userId);

    long countByReviewSessionUserId(Long userId);

    long countByReviewSessionUserIdAndStatus(Long userId, String status);
}
