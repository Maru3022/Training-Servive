package com.example.training_service.Controller;

import com.example.training_service.DTO.SetDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Service.TrainingService;
import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingBulkLoader bulkLoader;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public TrainingController(
            TrainingService trainingService,
            TrainingBulkLoader bulkLoader
    ) {
        this.trainingService = trainingService;
        this.bulkLoader = bulkLoader;
    }

    @PostMapping("/bulk-load")
    public ResponseEntity<String> startBulkLoad() {
        // Запускаем в новом потоке, чтобы не ждать окончания 1 млн записей в HTTP ответе
        new Thread(bulkLoader::runBulkLoad).start();
        return ResponseEntity.ok("Bulk loading started in background...");
    }

    @PostMapping
    public ResponseEntity<Training> postTrainings(
            @Valid @RequestBody TrainingDTO dto
    ) {
        log.info("REST request to create training: {}", dto.training_name());

        Training created = trainingService.createdTraining(dto);
        log.info("Created training for: {}", created);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainings(
            TrainingDTO dto,
            @PathVariable UUID id
    ) {
        log.info("Deleting exercise for training_id: {}", id);
        try {
            trainingService.deleteTraining(id);
            log.info("Successfully deleted exercise for training_id: {}", id);

            return ResponseEntity
                    .noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error deleting exercise for training_id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Training> getTrainings(
            @PathVariable UUID id
    ) {
        log.info("Getting training for id: {}", id);
        trainingService.getTraining(id);
        log.info("Get training for id: {}", id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(trainingService.getTraining(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTrainings(
            @Valid @RequestBody TrainingDTO dto,
            @PathVariable UUID id
    ) {
        log.info("Request to update training with id: {}. New Data: {} ", id, dto);

        Training updated = trainingService.updateFullTraining(id, dto);
        log.info("Successfully updated training with id: {} ", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(trainingService.getTraining(id));

    }

    @PatchMapping("/sets/{setId}")
    public ResponseEntity<ExerciseSet> patchSet(
            @PathVariable UUID setId,
            @Valid @RequestBody SetDTO set_dto
    ) {
        log.info("REST request to patch trainings for id: {}", setId);
        ExerciseSet updateSet = trainingService.patchSetPerformance(setId, set_dto);

        return ResponseEntity.ok(updateSet);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercises(
            @PathVariable UUID exerciseId
    ) {
        log.info("REST request to delete exercises for id: {}", exerciseId);
        trainingService.deleteSpecificTraining(exerciseId);

        return ResponseEntity.noContent().build();
    }
}