package com.example.training_service.saga;

import com.example.training_service.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCabinetResponse(CabinetResponseEvent response) {
        try {
            kafkaTemplate.send(KafkaTopics.SAGA_CABINET_RESPONSE, response.getCorrelationId(), response);
            log.info("Published saga.cabinet.response: correlationId={}, success={}", response.getCorrelationId(), response.isSuccess());
        } catch (Exception ex) {
            log.error("Failed to publish saga.cabinet.response for correlationId={}: {}", response.getCorrelationId(), ex.getMessage(), ex);
        }
    }
}
