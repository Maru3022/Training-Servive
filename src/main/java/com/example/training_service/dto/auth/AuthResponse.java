package com.example.training_service.dto.auth;

import com.example.training_service.dto.UserDTO;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        String refreshToken,
        UserDTO user
) {
}
