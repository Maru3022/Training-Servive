package com.example.training_service;

import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.TrainingStatus;
import com.example.training_service.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class TrainingBulkLoader {

    private static final Logger log = LoggerFactory.getLogger(TrainingBulkLoader.class);

    private final TrainingService trainingService;

    public TrainingBulkLoader(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    public void runBulkLoad(int count, int batchSize) {
        log.info("Starting bulk load: count={}, batchSize={}", count, batchSize);

        List<TrainingDTO> batch = new ArrayList<>(batchSize);
        int loaded = 0;

        for (int i = 0; i < count; i++) {
            TrainingDTO dto = TrainingDTO.builder()
                    .training_name("Bulk Training #" + (i + 1))
                    .training_date(LocalDate.now())
                    .user_id(UUID.randomUUID())
                    .training_status(TrainingStatus.PLANNED)
                    .sets(Collections.emptyList())
                    .build();
            batch.add(dto);

            if (batch.size() == batchSize || i == count - 1) {
                for (TrainingDTO t : batch) {
                    try {
                        trainingService.createdTrainingAsync(t);
                        loaded++;
                    } catch (Exception ex) {
                        log.error("Failed to create training during bulk load: {}", ex.getMessage(), ex);
                    }
                }
                batch.clear();
                log.info("Bulk load progress: {}/{}", loaded, count);
            }
        }

        log.info("Bulk load completed: {} training(s) submitted", loaded);
    }
}
