package com.example.training_service.controller;

import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import com.example.training_service.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
@Tag(name = "Training API", description = "Manage training sessions, set performance updates, and bulk loading operations.")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingBulkLoader trainingBulkLoader;

    public TrainingController(TrainingService trainingService, TrainingBulkLoader trainingBulkLoader) {
        this.trainingService = trainingService;
        this.trainingBulkLoader = trainingBulkLoader;
    }

    @PostMapping("/bulk-load")
    @Operation(summary = "Trigger a bulk load job", description = "Starts placeholder bulk creation logic for performance or seeding scenarios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk load accepted for execution")
    })
    public ResponseEntity<String> triggerBulkLoad(@RequestParam int count, @RequestParam int batchSize) {
        trainingBulkLoader.runBulkLoad(count, batchSize);
        return ResponseEntity.ok("Bulk load triggered");
    }

    @PostMapping
    @Operation(
            summary = "Create a training",
            description = "Creates a training asynchronously and returns a tracking identifier.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TrainingDTO.class),
                            examples = @ExampleObject(
                                    name = "Strength block request",
                                    value = """
                                            {
                                              "training_date": "2026-04-17",
                                              "user_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                              "training_name": "Strength Block A",
                                              "training_status": "PLANNED",
                                              "sets": [
                                                {
                                                  "exercise_id": "550e8400-e29b-41d4-a716-446655440000",
                                                  "weight": 80,
                                                  "reps": 8,
                                                  "order_": 1
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Training accepted for asynchronous creation"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<UUID> postTrainings(@Valid @RequestBody TrainingDTO trainingDTO) {
        UUID id = trainingService.createdTrainingAsync(trainingDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a training by id", description = "Returns the current training snapshot for a given identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training returned successfully")
    })
    public ResponseEntity<Training> getTraining(@PathVariable UUID id) {
        Training training = trainingService.getTraining(id);
        return ResponseEntity.ok(training);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a training", description = "Performs a full update of a training resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<Training> updateTraining(@PathVariable UUID id, @Valid @RequestBody TrainingDTO trainingDTO) {
        Training updated = trainingService.updateFullTraining(id, trainingDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a training", description = "Removes the training resource identified by the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Training deleted successfully")
    })
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID id) {
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sets/{setId}")
    @Operation(summary = "Patch set performance", description = "Partially updates the performance details of a single exercise set.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Set updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<ExerciseSet> patchSet(@PathVariable UUID setId, @Valid @RequestBody SetDTO setDTO) {
        ExerciseSet patchedSet = trainingService.patchSetPerformance(setId, setDTO);
        return ResponseEntity.ok(patchedSet);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    @Operation(summary = "Delete an exercise", description = "Removes a specific exercise entry by identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise deleted successfully")
    })
    public ResponseEntity<Void> deleteExercise(@PathVariable UUID exerciseId) {
        trainingService.deleteSpecificTraining(exerciseId);
        return ResponseEntity.noContent().build();
    }
}
