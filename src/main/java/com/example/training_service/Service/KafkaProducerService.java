package com.example.training_service.Service;

import com.example.training_service.DTO.TrainingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, TrainingDTO> kafkaTemplate;
    private static final String TOPIC = "training-events";

    public void sendTrainingEvent(TrainingDTO dto) {
        // Используем userId как ключ, чтобы события одного юзера попадали в одну партицию
        String key = String.valueOf(dto.userId());

        // В Spring Boot 3 send() возвращает CompletableFuture
        CompletableFuture<SendResult<String, TrainingDTO>> future = kafkaTemplate.send(TOPIC, key, dto);

        // Обрабатываем результат асинхронно
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send message to Kafka: key={}, error={}", key, ex.getMessage());
            }
        });
    }
}