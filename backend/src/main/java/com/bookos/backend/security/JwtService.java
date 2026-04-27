package com.bookos.backend.security;

import com.bookos.backend.config.JwtProperties;
import com.bookos.backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    private SecretKey secretKey;

    @PostConstruct
    void initialize() {
        String configuredSecret = properties.getSecret();
        if (!StringUtils.hasText(configuredSecret)) {
            byte[] randomBytes = new byte[64];
            new SecureRandom().nextBytes(randomBytes);
            configuredSecret = Base64.getEncoder().encodeToString(randomBytes);
            log.warn("JWT_SECRET is not configured. Generated an in-memory development secret for this process.");
        }

        secretKey = Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.getExpirationMinutes() * 60);

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(properties.getIssuer())
                .claim("role", user.getRole().getName().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, String expectedSubject) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equalsIgnoreCase(expectedSubject) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
