package com.example.training_service.service.schedule;

import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.OutboxEvent;
import com.example.training_service.model.OutboxStatus;
import com.example.training_service.repository.OutboxRepository;
import com.example.training_service.service.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxSchedule {

    private final OutboxRepository outboxRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents(){
        List<OutboxEvent> pendingEvent = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if(pendingEvent.isEmpty()) return;

        log.info("Outbox worker found {} pending events to process", pendingEvent);

        for(OutboxEvent event : pendingEvent){
            try{
                TrainingDTO dto = objectMapper.readValue(event.getPayload(), TrainingDTO.class);

                kafkaProducerService.sendTrainingEvent(dto);
                event.setStatus(OutboxStatus.PROCESSED);
                log.info("Event successfully relayed to Kafka. AggregateID: {}", event.getAggregateId());
            }catch(Exception e) {
                log.error("Failed to relay outbox event ID {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxStatus.FAILED);
            }
        }

        outboxRepository.saveAll(pendingEvent);
    }
}
