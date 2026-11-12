package com.example.training_service.service;

import com.example.training_service.exception.AuthenticationException;
import com.example.training_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final String issuer;

    public JwtTokenService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${auth.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${auth.jwt.issuer}") String issuer) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    public String createAccessToken(User user) {
        return createToken(user, "access", accessExpirationMs);
    }

    public String createRefreshToken(User user) {
        return createToken(user, "refresh", refreshExpirationMs);
    }

    public UUID parseRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).requireIssuer(issuer).build()
                    .parseSignedClaims(token).getPayload();
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw new AuthenticationException("Invalid refresh token");
            }
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    private String createToken(User user, String type, long lifetimeMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(lifetimeMs)))
                .signWith(signingKey)
                .compact();
    }
}
