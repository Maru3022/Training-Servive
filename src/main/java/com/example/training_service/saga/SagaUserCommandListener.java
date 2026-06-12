package com.example.training_service.saga;

import com.example.training_service.outbox.OutboxEvent;
import com.example.training_service.outbox.OutboxEventRepository;
import com.example.training_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaUserCommandListener {

    private final UserService userService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "saga-user-command", containerFactory = "sagaKafkaListenerContainerFactory",
            groupId = "training-service-saga")
    @Transactional
    public void onCommand(SagaCommandEvent event) {
        log.info("Received saga-user-command: sagaId={}, status={}", event.getSagaId(), event.getStatus());

        if (!"ROLLBACK".equals(event.getStatus())) {
            return; // USER не имеет forward-команды
        }

        try {
            Object userIdRaw = event.getData() != null ? event.getData().get("userId") : null;
            if (userIdRaw == null) {
                log.error("ROLLBACK saga-user-command without userId, sagaId={}", event.getSagaId());
                return;
            }
            UUID userId = UUID.fromString(userIdRaw.toString());
            userService.softDelete(userId);
            log.info("User {} soft-deleted as compensation for saga {}", userId, event.getSagaId());
        } catch (Exception e) {
            log.error("Failed to compensate USER step for saga {}: {}", event.getSagaId(), e.getMessage(), e);
        }

        publishResponse(event, "ROLLBACK_DONE", null);
    }

    private void publishResponse(SagaCommandEvent command, String status, Map<String, Object> data) {
        try {
            SagaResponseEvent response = new SagaResponseEvent();
            response.setEventId(UUID.randomUUID().toString());
            response.setSagaId(command.getSagaId());
            response.setStep("USER");
            response.setStatus(status);
            response.setData(data);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setTopic("saga-user-response");
            outboxEvent.setKey(command.getSagaId());
            outboxEvent.setPayload(objectMapper.writeValueAsString(response));
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to publish saga-user-response: {}", e.getMessage(), e);
        }
    }
}