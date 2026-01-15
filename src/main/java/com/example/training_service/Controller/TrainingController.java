package com.example.training_service.Controller;

import com.example.training_service.DTO.ExerciseDTO;
import com.example.training_service.DTO.SetDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Service.TrainingService;
import com.example.training_service.model.Exercise;
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

    @DeleteMapping("/{id}/exercise")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable UUID id,
            ExerciseDTO dto
    ) {
        log.info("Deleting exercise for training_id: {}", id);
        try {
            trainingService.deleteExercise(id);
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

    @GetMapping("/{id}/delete")
    public ResponseEntity<Void> getExercise(
            @PathVariable UUID id,
            ExerciseDTO dto
    ) {
        log.info("Getting exercise for id: {}", id);
        try {
            trainingService.getExercise(id, dto);
            log.info("Get exercise for id: {}", id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error getting exercise for id: {}. Reason: {}", id, e.getMessage());
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

    @PutMapping("/{id}/exercise")
    public ResponseEntity<Exercise> updateExercise(
            @PathVariable UUID id,
            @Valid @RequestBody ExerciseDTO dto
    ) {
        log.info("Request to update with id: {}. New Data: {}", id, dto);
        try {
            trainingService.updateExercise(id, dto);
            log.info("Successfully updated exercise with id: {} ", id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(trainingService.getExercise(id, dto));
        } catch (Exception e) {
            log.error("Error updating exercise with id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}/exercise/set")
    public ResponseEntity<ExerciseSet> getExerciseSet(
            @PathVariable UUID id,
            @Valid @RequestBody SetDTO set_dto
    ) {
        log.info("Getting exercise set for id: {}", id, set_dto);
        try {
            getExerciseSet(id, set_dto);
            log.info("Successfull get exercise set for id: {} ", id);

            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(trainingService.getExerciseSet(id));
        } catch (Exception e) {
            log.error("Error getting exercise set for id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/exercise/set")
    public ResponseEntity<ExerciseSet> postExerciseSet(
            @PathVariable UUID id,
            @Valid @RequestBody SetDTO set_dto
    ) {
        ExerciseSet exerciseSet = new ExerciseSet();

        exerciseSet.setId(id);
        exerciseSet.setExercise_id(set_dto.exercise_id());
        exerciseSet.setWeight(set_dto.weight());
        exerciseSet.setReps(set_dto.reps());
        exerciseSet.setOrder(set_dto.order());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(exerciseSet);
    }

    @PutMapping("/{id}/exercise/set")
    public ResponseEntity<ExerciseSet> updateExerciseSet(
            @PathVariable UUID id,
            @Valid @RequestBody SetDTO set_dto
    ) {
        log.info("Request to update for id: {}. New Data: {} ", id, set_dto);
        try {
            trainingService.updateExerciseSet(id, set_dto);
            log.info("Successfully updated exercise set for id: {} ", id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(trainingService.getExerciseSet(id));
        } catch (Exception e) {
            log.error("Error updating exercise set for id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{id}/exercise/set")
    public ResponseEntity<ExerciseSet> deleteExerciseSet(
            @PathVariable UUID id,
            @Valid @RequestBody SetDTO set_dto
    ) {
        log.info("Request to delete for id: {} ", id);
        try {
            trainingService.deleteExerciseSet(id, set_dto);
            log.info("Successfully deleted exercise set for id: {} ", id);
            return ResponseEntity
                    .noContent()
                    .build();
        } catch (Exception e) {
            log.error("Error deleting exercise set for id: {}. Reason: {}", id, e.getMessage());
            throw e;
        }
    }
}