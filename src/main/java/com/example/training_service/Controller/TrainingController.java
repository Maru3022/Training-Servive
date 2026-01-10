package com.example.training_service.Controller;

import com.example.training_service.DTO.ExerciseDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Service.TrainingService;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.Training;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//ToDo: Exercise @GetMapping, @DeleteMapping, @PutMapping

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public TrainingController(
            TrainingService trainingService
    ) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public ResponseEntity<Training> postTrainings(
            @Valid @RequestBody TrainingDTO dto
    ) {
        log.info("Posting training for: {}", dto);
        try {
            Training created = trainingService.createdTraining(dto);
            log.info("Created training for: {}", created);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);
        } catch (Exception e) {
            log.error("Error while creating training. Reason: {}", e.getMessage());
            throw e;
        }

    }

    @PostMapping("/{id}/exercise")
    public ResponseEntity<Exercise> createExercise(
            @Valid @RequestBody ExerciseDTO dto,
            @PathVariable UUID trainingId
    ) {
        log.info("Creating exercise for training_id: {}", trainingId);
        try {
            Exercise exercise1 = trainingService.addExercise(trainingId, dto);
            log.info("Created exercise for training_id: {}", trainingId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(exercise1);
        } catch (Exception e) {
            log.error("Error creating for training_id: {}. Reason: {}", trainingId, e.getMessage());
            throw e;
        }

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
        try {
            trainingService.getTraining(id);
            log.info("Get training for id: {}", id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(trainingService.getTraining(id));
        } catch (Exception e) {
            log.error("Error getting training for id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTrainings(
            @Valid @RequestBody TrainingDTO dto,
            @PathVariable UUID id
    ) {
        log.info("Request to update training with id: {}. New Data: {} ", id, dto);

        try {
            trainingService.updateTraining(id, dto);
            log.info("Successfully updated training with id: {} ", id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(trainingService.getTraining(id));
        } catch (Exception e) {
            log.error("Error updating training with id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }
}