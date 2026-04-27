package com.bookos.backend.user.service;

import com.bookos.backend.common.enums.RoleName;
import com.bookos.backend.user.dto.CurrentUserResponse;
import com.bookos.backend.user.dto.UserAdminResponse;
import com.bookos.backend.user.dto.UserProfileResponse;
import com.bookos.backend.user.entity.Role;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.repository.RoleRepository;
import com.bookos.backend.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public User getByEmailRequired(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(String email) {
        return toCurrentUserResponse(getByEmailRequired(email));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentProfile(String email) {
        User user = getByEmailRequired(email);
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getProfile().getDisplayName(),
                user.getProfile().getBio(),
                user.getRole().getName());
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> listUsers() {
        return userRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(user -> new UserAdminResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getProfile().getDisplayName(),
                        user.getRole().getName(),
                        user.isEnabled()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Role getRequiredRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new NoSuchElementException("Role %s not found.".formatted(roleName)));
    }

    public CurrentUserResponse toCurrentUserResponse(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getProfile().getDisplayName(),
                user.getRole().getName());
    }
}
