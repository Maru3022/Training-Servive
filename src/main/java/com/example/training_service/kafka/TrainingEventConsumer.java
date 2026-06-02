package com.example.training_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TrainingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TrainingEventConsumer.class);

    @KafkaListener(topics = KafkaTopics.TRAINING_CREATED, groupId = "training-service-group")
    public void consumeTrainingCreated(Object event) {
        log.info("Received TrainingCreatedEvent: {}", event);
        // Downstream processing stub — e.g. notify analytics service, send welcome email, etc.
    }

    @KafkaListener(topics = KafkaTopics.TRAINING_STATUS_CHANGED, groupId = "training-service-group")
    public void consumeTrainingStatusChanged(Object event) {
        log.info("Received TrainingStatusChangedEvent: {}", event);
        // Downstream processing stub — e.g. update leaderboard, push notification, etc.
    }
}
