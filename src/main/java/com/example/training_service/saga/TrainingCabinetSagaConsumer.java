package com.example.training_service.saga;

import com.example.training_service.kafka.KafkaTopics;
import com.example.training_service.model.TrainingCabinet;
import com.example.training_service.model.TrainingCabinetStatus;
import com.example.training_service.repository.TrainingCabinetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingCabinetSagaConsumer {

    private final TrainingCabinetRepository cabinetRepository;
    private final SagaKafkaProducer kafkaProducer;

    @KafkaListener(topics = KafkaTopics.SAGA_CABINET_CREATE, containerFactory = "sagaKafkaListenerContainerFactory",
            groupId = "training-service-saga")
    @Transactional
    public void handleCreateCabinet(CabinetCommandEvent event, Acknowledgment ack, ConsumerRecord<String, CabinetCommandEvent> record) {
        log.info("Received saga.cabinet.create: correlationId={}, userId={}", event.getCorrelationId(), event.getUserId());
        try {
            if (event.getCorrelationId() == null || event.getUserId() == null) {
                throw new IllegalArgumentException("correlationId and userId are required");
            }

            if (cabinetRepository.existsByCorrelationId(event.getCorrelationId())) {
                log.info("Idempotent create ignored for correlationId={}", event.getCorrelationId());
                kafkaProducer.publishCabinetResponse(CabinetResponseEvent.builder()
                        .correlationId(event.getCorrelationId())
                        .userId(event.getUserId())
                        .cabinetId(cabinetRepository.findByCorrelationId(event.getCorrelationId())
                                .map(TrainingCabinet::getId)
                                .orElse(null))
                        .success(true)
                        .build());
                ack.acknowledge();
                return;
            }

            if (cabinetRepository.existsByUserId(event.getUserId())) {
                log.warn("User {} already has a cabinet, correlationId={}", event.getUserId(), event.getCorrelationId());
            }

            TrainingCabinet cabinet = TrainingCabinet.builder()
                    .correlationId(event.getCorrelationId())
                    .userId(event.getUserId())
                    .status(TrainingCabinetStatus.ACTIVE)
                    .build();

            TrainingCabinet savedCabinet = cabinetRepository.save(cabinet);
            kafkaProducer.publishCabinetResponse(CabinetResponseEvent.builder()
                    .correlationId(event.getCorrelationId())
                    .userId(event.getUserId())
                    .cabinetId(savedCabinet.getId())
                    .success(true)
                    .build());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to create training cabinet for correlationId={}: {}", event.getCorrelationId(), ex.getMessage(), ex);
            kafkaProducer.publishCabinetResponse(CabinetResponseEvent.builder()
                    .correlationId(event != null ? event.getCorrelationId() : null)
                    .userId(event != null ? event.getUserId() : null)
                    .success(false)
                    .errorMessage(ex.getMessage())
                    .build());
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }

    @KafkaListener(topics = KafkaTopics.SAGA_CABINET_COMPENSATE, containerFactory = "sagaKafkaListenerContainerFactory",
            groupId = "training-service-saga")
    @Transactional
    public void handleCompensation(CompensationEvent event, Acknowledgment ack) {
        log.info("Received saga.cabinet.compensate: correlationId={}, userId={}", event.getCorrelationId(), event.getUserId());
        try {
            cabinetRepository.findByCorrelationId(event.getCorrelationId())
                    .ifPresent(cabinet -> {
                        cabinet.setStatus(TrainingCabinetStatus.DELETED);
                        cabinetRepository.save(cabinet);
                        log.info("Compensated training cabinet {} for correlationId={}", cabinet.getId(), event.getCorrelationId());
                    });
        } catch (Exception ex) {
            log.error("Failed to compensate training cabinet for correlationId={}: {}", event.getCorrelationId(), ex.getMessage(), ex);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
