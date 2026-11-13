package com.example.training_service.controller;

import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.exception.ErrorResponse;
import com.example.training_service.model.TrainingStatus;
import com.example.training_service.service.TrainingService;
import com.example.training_service.security.RequestIdentity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainings")
@Tag(name = "Trainings", description = "Training session management")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingBulkLoader trainingBulkLoader;

    public TrainingController(TrainingService trainingService, TrainingBulkLoader trainingBulkLoader) {
        this.trainingService = trainingService;
        this.trainingBulkLoader = trainingBulkLoader;
    }

    @Operation(summary = "Trigger bulk load", description = "Triggers an asynchronous bulk load of training records")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk load triggered")
    })
    @PostMapping("/bulk-load")
    public ResponseEntity<String> triggerBulkLoad(
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Total number of records to load", required = true) @RequestParam int count,
            @Parameter(description = "Number of records per batch", required = true) @RequestParam int batchSize) {
        RequestIdentity.requireAdmin(role);
        trainingBulkLoader.runBulkLoad(count, batchSize);
        return ResponseEntity.ok("Bulk load triggered");
    }

    @Operation(summary = "Create training", description = "Creates a new training session and publishes a Kafka event")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Training creation accepted, returns the new training ID"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UUID> postTrainings(
            @RequestHeader("X-User-Id") String authenticatedId,
            @Valid @RequestBody TrainingDTO trainingDTO) {
        trainingDTO.setUser_id(RequestIdentity.userId(authenticatedId));
        UUID id = trainingService.createTraining(trainingDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(id);
    }

    @Operation(summary = "Get training by ID", description = "Returns a training session by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training found",
                    content = @Content(schema = @Schema(implementation = TrainingDTO.class))),
            @ApiResponse(responseCode = "404", description = "Training not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TrainingDTO> getTraining(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Training UUID", required = true) @PathVariable UUID id) {
        TrainingDTO training = trainingService.getTraining(id);
        RequestIdentity.requireOwnerOrAdmin(training.getUser_id(), authenticatedId, role);
        return ResponseEntity.ok(training);
    }

    @Operation(summary = "Get filtered trainings", description = "Returns a paginated list of training sessions filtered by user, status, and/or date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<TrainingDTO>> getTrainings(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Filter by user UUID") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter by training status") @RequestParam(required = false) TrainingStatus status,
            @Parameter(description = "Start date (inclusive), format: yyyy-MM-dd") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive), format: yyyy-MM-dd") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "training_date") Pageable pageable) {
        UUID effectiveUserId = "ADMIN".equals(role) && userId != null
                ? userId : RequestIdentity.userId(authenticatedId);
        return ResponseEntity.ok(trainingService.getFilteredTrainings(effectiveUserId, status, from, to, pageable));
    }

    @Operation(summary = "Full update training", description = "Completely replaces a training session's data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training updated",
                    content = @Content(schema = @Schema(implementation = TrainingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Training not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TrainingDTO> updateTraining(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Training UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody TrainingDTO trainingDTO) {
        TrainingDTO existing = trainingService.getTraining(id);
        RequestIdentity.requireOwnerOrAdmin(existing.getUser_id(), authenticatedId, role);
        trainingDTO.setUser_id(existing.getUser_id());
        return ResponseEntity.ok(trainingService.updateFullTraining(id, trainingDTO));
    }

    @Operation(summary = "Delete training", description = "Permanently removes a training session and its exercise sets")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Training deleted"),
            @ApiResponse(responseCode = "404", description = "Training not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Training UUID", required = true) @PathVariable UUID id) {
        RequestIdentity.requireOwnerOrAdmin(trainingService.getTraining(id).getUser_id(), authenticatedId, role);
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Patch exercise set performance", description = "Partially updates an exercise set's weight, reps, or order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Set updated",
                    content = @Content(schema = @Schema(implementation = SetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Set not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/sets/{setId}")
    public ResponseEntity<SetDTO> patchSet(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Exercise set UUID", required = true) @PathVariable UUID setId,
            @RequestBody SetDTO setDTO) {
        RequestIdentity.requireOwnerOrAdmin(trainingService.getSetOwnerId(setId), authenticatedId, role);
        return ResponseEntity.ok(trainingService.patchSetPerformance(setId, setDTO));
    }

    @Operation(summary = "Delete exercise set", description = "Removes a specific exercise set from a training session")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise set deleted"),
            @ApiResponse(responseCode = "404", description = "Exercise set not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @RequestHeader("X-User-Id") String authenticatedId,
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "Exercise set UUID", required = true) @PathVariable UUID exerciseId) {
        RequestIdentity.requireOwnerOrAdmin(trainingService.getSetOwnerId(exerciseId), authenticatedId, role);
        trainingService.deleteSpecificTraining(exerciseId);
        return ResponseEntity.noContent().build();
    }
}
