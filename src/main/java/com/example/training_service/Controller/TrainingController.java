package com.example.training_service.Controller;

import com.example.training_service.DTO.SetDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Service.TrainingService;
import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingBulkLoader bulkLoader;
    private final Logger log = LoggerFactory.getLogger(TrainingController.class);

    public TrainingController(
            TrainingService trainingService,
            TrainingBulkLoader bulkLoader
    ) {
        this.trainingService = trainingService;
        this.bulkLoader = bulkLoader;
    }

    @PostMapping("/bulk-load")
    public ResponseEntity<String> startBulkLoad(
            @RequestParam(defaultValue = "1000000") int count,
            @RequestParam(defaultValue = "5000") int batchSize
    ) {
        new Thread(() -> bulkLoader.runBulkLoad(count, batchSize)).start();
        return ResponseEntity.ok("Bulk loading started in background...");
    }

    @PostMapping
    public ResponseEntity<Training> postTrainings(
            @Valid @RequestBody TrainingDTO dto
    ) {
        log.info("REST request to create training: {}", dto.training_name());
        Training created = trainingService.createdTraining(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Training> getTraining(
            @PathVariable UUID id
    ) {
        log.info("REST request to get training with id: {}", id);
        Training training = trainingService.getTraining(id);

        return ResponseEntity.ok(training);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTraining(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingDTO dto
    ) {
        log.info("REST request to update training with id: {}", id);
        Training updated = trainingService.updateFullTraining(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete training with id: {}", id);
        trainingService.deleteTraining(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sets/{setId}")
    public ResponseEntity<ExerciseSet> patchSet(
            @PathVariable UUID setId,
            @Valid @RequestBody SetDTO dto
    ) {
        log.info("REST request to patch set with id: {}", setId);
        ExerciseSet updatedSet = trainingService.patchSetPerformance(setId, dto);

        return ResponseEntity.ok(updatedSet);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable UUID exerciseId
    ) {
        log.info("REST request to delete exercise with id: {}", exerciseId);
        trainingService.deleteSpecificTraining(exerciseId);

        return ResponseEntity.noContent().build();
    }
}
