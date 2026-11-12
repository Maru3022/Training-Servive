package com.example.training_service.event;

import java.util.List;
import java.util.UUID;

public record TrainingNotificationEvent(
        UUID userId,
        String telegramTag,
        String training_name,
        String data,
        String status,
        String email,
        List<Object> exercises
) {
}
