package com.example.training_service.Service;

import com.example.training_service.DTO.TrainingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final TrainingService trainingService;

    /**
     * concurrency = 10 позволяет обрабатывать 10 батчей параллельно.
     * Для этого мы ранее создали 20 партиций в топике.
     */
    @KafkaListener(
            topics = "training-events",
            groupId = "training-group",
            concurrency = "10",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(List<TrainingDTO> dtos, Acknowledgment ack) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        log.info("Received batch from Kafka. Size: {}", dtos.size());

        try {
            // Вызываем метод сохранения пачки (в TrainingService он должен быть @Transactional)
            trainingService.saveBatch(dtos);

            // Ручное подтверждение прочтения (Offset commit)
            ack.acknowledge();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch of {} saved to DB in {}ms", dtos.size(), duration);

        } catch (Exception e) {
            log.error("CRITICAL: Error saving batch to database. Batch will be retried. Error: {}", e.getMessage());
            // Мы НЕ вызываем ack.acknowledge(), чтобы Kafka переподала эти сообщения позже
        }
    }
}