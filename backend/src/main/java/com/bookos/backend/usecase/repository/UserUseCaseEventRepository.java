package com.bookos.backend.usecase.repository;

import com.bookos.backend.usecase.entity.UseCaseEventType;
import com.bookos.backend.usecase.entity.UserUseCaseEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUseCaseEventRepository extends JpaRepository<UserUseCaseEvent, Long> {

    long countByUserIdAndEventType(Long userId, UseCaseEventType eventType);
}
