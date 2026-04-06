package com.example.training_service.controller;

import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import com.example.training_service.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingBulkLoader trainingBulkLoader;

    public TrainingController(TrainingService trainingService, TrainingBulkLoader trainingBulkLoader) {
        this.trainingService = trainingService;
        this.trainingBulkLoader = trainingBulkLoader;
    }

    @PostMapping("/bulk-load")
    public ResponseEntity<String> triggerBulkLoad(@RequestParam int count, @RequestParam int batchSize) {
        trainingBulkLoader.runBulkLoad(count, batchSize);
        return ResponseEntity.ok("Bulk load triggered");
    }

    @PostMapping
    public ResponseEntity<UUID> postTrainings(@RequestBody TrainingDTO trainingDTO) {
        UUID id = trainingService.createdTrainingAsync(trainingDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Training> getTraining(@PathVariable UUID id) {
        Training training = trainingService.getTraining(id);
        return ResponseEntity.ok(training);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTraining(@PathVariable UUID id, @RequestBody TrainingDTO trainingDTO) {
        Training updated = trainingService.updateFullTraining(id, trainingDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID id) {
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sets/{setId}")
    public ResponseEntity<ExerciseSet> patchSet(@PathVariable UUID setId, @RequestBody SetDTO setDTO) {
        ExerciseSet patchedSet = trainingService.patchSetPerformance(setId, setDTO);
        return ResponseEntity.ok(patchedSet);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(@PathVariable UUID exerciseId) {
        trainingService.deleteSpecificTraining(exerciseId);
        return ResponseEntity.noContent().build();
    }
}
