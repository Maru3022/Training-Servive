package com.example.training_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {
    private String eventId;
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private long occurredAt;
}