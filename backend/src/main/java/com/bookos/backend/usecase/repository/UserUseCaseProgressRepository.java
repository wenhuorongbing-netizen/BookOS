package com.bookos.backend.usecase.repository;

import com.bookos.backend.usecase.entity.UserUseCaseProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUseCaseProgressRepository extends JpaRepository<UserUseCaseProgress, Long> {

    List<UserUseCaseProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<UserUseCaseProgress> findByUserIdAndUseCaseSlug(Long userId, String useCaseSlug);

    void deleteByUserIdAndUseCaseSlug(Long userId, String useCaseSlug);
}
