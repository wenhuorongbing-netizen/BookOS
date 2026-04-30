package com.bookos.backend.usecase.service;

import com.bookos.backend.usecase.dto.UseCaseEventRequest;
import com.bookos.backend.usecase.dto.UseCaseEventResponse;
import com.bookos.backend.usecase.entity.UserUseCaseEvent;
import com.bookos.backend.usecase.repository.UserUseCaseEventRepository;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UseCaseEventService {

    private final UserService userService;
    private final UserUseCaseEventRepository eventRepository;

    @Transactional
    public UseCaseEventResponse record(String email, UseCaseEventRequest request) {
        User user = userService.getByEmailRequired(email);
        UserUseCaseEvent event = new UserUseCaseEvent();
        event.setUser(user);
        event.setEventType(request.eventType());
        event.setContextType(normalizeNullable(request.contextType()));
        event.setContextId(normalizeNullable(request.contextId()));
        return toResponse(eventRepository.save(event));
    }

    private static UseCaseEventResponse toResponse(UserUseCaseEvent event) {
        return new UseCaseEventResponse(
                event.getId(),
                event.getEventType(),
                event.getContextType(),
                event.getContextId(),
                event.getCreatedAt());
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
