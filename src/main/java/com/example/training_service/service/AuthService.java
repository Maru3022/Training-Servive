package com.example.training_service.service;

import com.example.training_service.dto.auth.AuthResponse;
import com.example.training_service.dto.auth.LoginRequest;
import com.example.training_service.dto.auth.RegisterRequest;
import com.example.training_service.exception.AuthenticationException;
import com.example.training_service.model.User;
import com.example.training_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, UserService userService,
                       PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerUser(request, passwordEncoder.encode(request.password()));
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(request.login(), request.login())
                .orElseThrow(() -> new AuthenticationException("Invalid login or password"));
        if (!user.isActive() || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid login or password");
        }
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        UUID userId = jwtTokenService.parseRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new AuthenticationException("User is inactive or no longer exists"));
        return responseFor(user);
    }

    private AuthResponse responseFor(User user) {
        return new AuthResponse(
                "Bearer",
                jwtTokenService.createAccessToken(user),
                jwtTokenService.getAccessExpirationSeconds(),
                jwtTokenService.createRefreshToken(user),
                userService.toDTO(user));
    }
}
