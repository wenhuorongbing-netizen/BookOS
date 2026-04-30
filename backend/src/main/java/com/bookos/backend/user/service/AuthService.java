package com.bookos.backend.user.service;

import com.bookos.backend.common.enums.RoleName;
import com.bookos.backend.security.JwtService;
import com.bookos.backend.user.dto.AuthResponse;
import com.bookos.backend.user.dto.LoginRequest;
import com.bookos.backend.user.dto.RegisterRequest;
import com.bookos.backend.user.entity.User;
import com.bookos.backend.user.entity.UserProfile;
import com.bookos.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(userService.getRequiredRole(RoleName.USER));

        UserProfile profile = new UserProfile();
        profile.setDisplayName(request.displayName().trim());
        profile.setOnboardingCompleted(false);
        user.setProfile(profile);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, userService.toCurrentUserResponse(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, userService.toCurrentUserResponse(user));
    }
}
