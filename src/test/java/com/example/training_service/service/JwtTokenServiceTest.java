package com.example.training_service.service;

import com.example.training_service.exception.AuthenticationException;
import com.example.training_service.model.Role;
import com.example.training_service.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenServiceTest {
    private static final String SECRET = "test-only-jwt-secret-with-at-least-32-bytes";

    private final JwtTokenService tokens = new JwtTokenService(SECRET, 60_000, 120_000, "fitflow-test");

    @Test
    void refreshTokenResolvesUserId() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("athlete")
                .role(Role.ATHLETE)
                .build();

        assertEquals(user.getId(), tokens.parseRefreshToken(tokens.createRefreshToken(user)));
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("athlete")
                .role(Role.ATHLETE)
                .build();

        assertThrows(AuthenticationException.class,
                () -> tokens.parseRefreshToken(tokens.createAccessToken(user)));
    }
}
