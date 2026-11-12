package com.example.training_service.security;

import com.example.training_service.exception.AccessDeniedException;

import java.util.UUID;

public final class RequestIdentity {
    private RequestIdentity() {
    }

    public static UUID userId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid authenticated user identity");
        }
    }

    public static void requireOwnerOrAdmin(UUID ownerId, String authenticatedId, String role) {
        if (!"ADMIN".equals(role) && !ownerId.equals(userId(authenticatedId))) {
            throw new AccessDeniedException("You cannot access another user's data");
        }
    }

    public static void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("Administrator role is required");
        }
    }
}
