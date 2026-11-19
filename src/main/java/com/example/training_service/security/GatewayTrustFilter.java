package com.example.training_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Profile("!test")
public class GatewayTrustFilter extends OncePerRequestFilter {
    static final String SECRET_HEADER = "X-Internal-Gateway-Secret";
    private final String expectedSecret;

    public GatewayTrustFilter(@Value("${internal.gateway.secret:}") String expectedSecret) {
        this.expectedSecret = expectedSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (hasIdentity(request) && !trusted(request.getHeader(SECRET_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasIdentity(HttpServletRequest request) {
        return request.getHeader("X-User-Id") != null || request.getHeader("X-User-Role") != null;
    }

    private boolean trusted(String supplied) {
        return supplied != null && !expectedSecret.isBlank() && MessageDigest.isEqual(
                expectedSecret.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }
}
