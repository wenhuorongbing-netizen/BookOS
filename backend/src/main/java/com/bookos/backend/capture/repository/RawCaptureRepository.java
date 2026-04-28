package com.bookos.backend.capture.repository;

import com.bookos.backend.capture.entity.RawCapture;
import com.bookos.backend.common.enums.CaptureStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawCaptureRepository extends JpaRepository<RawCapture, Long> {

    @EntityGraph(attributePaths = {"book"})
    Optional<RawCapture> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"book"})
    List<RawCapture> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, CaptureStatus status);

    @EntityGraph(attributePaths = {"book"})
    List<RawCapture> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book"})
    List<RawCapture> findByUserIdAndBookIdOrderByCreatedAtDesc(Long userId, Long bookId);

    @EntityGraph(attributePaths = {"book"})
    List<RawCapture> findByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(Long userId, Long bookId, CaptureStatus status);

    @EntityGraph(attributePaths = {"book"})
    List<RawCapture> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, CaptureStatus status);
}
