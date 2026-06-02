package com.example.training_service.kafka;

import com.example.training_service.event.TrainingCreatedEvent;
import com.example.training_service.event.TrainingStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnExpression("!'${spring.kafka.bootstrap-servers:}'.trim().isEmpty()")
public class TrainingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TrainingEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TrainingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTrainingCreated(TrainingCreatedEvent event) {
        String key = event.getTrainingId().toString();
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.TRAINING_CREATED, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send TrainingCreatedEvent for trainingId={}: {}",
                        event.getTrainingId(), ex.getMessage(), ex);
            } else {
                log.info("TrainingCreatedEvent sent successfully for trainingId={}, offset={}",
                        event.getTrainingId(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    public void sendTrainingStatusChanged(TrainingStatusChangedEvent event) {
        String key = event.getTrainingId().toString();
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaTopics.TRAINING_STATUS_CHANGED, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send TrainingStatusChangedEvent for trainingId={}: {}",
                        event.getTrainingId(), ex.getMessage(), ex);
            } else {
                log.info("TrainingStatusChangedEvent sent successfully for trainingId={}, offset={}",
                        event.getTrainingId(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
