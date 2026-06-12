package com.example.training_service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAt(Status.PENDING);
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload()).get();
                event.setStatus(Status.SENT);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("Outbox event {} sent to {}", event.getId(), event.getTopic());
            } catch (Exception e) {
                log.error("Error sending outbox event {}: {}", event.getId(), e.getMessage());
                event.setStatus(Status.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}