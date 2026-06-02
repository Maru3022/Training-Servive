package com.example.training_service.controller;

import com.example.training_service.dto.ExerciseDTO;
import com.example.training_service.exception.ErrorResponse;
import com.example.training_service.model.MuscleGroup;
import com.example.training_service.service.ExerciseService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/exercises")
@Tag(name = "Exercises", description = "Exercise catalogue management")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "Create exercise", description = "Adds a new exercise to the catalogue")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercise created",
                    content = @Content(schema = @Schema(implementation = ExerciseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ExerciseDTO> createExercise(@Valid @RequestBody ExerciseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(dto));
    }

    @Operation(summary = "Get exercise by ID", description = "Returns a single exercise by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise found",
                    content = @Content(schema = @Schema(implementation = ExerciseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Exercise not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> getExercise(
            @Parameter(description = "Exercise UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(exerciseService.getById(id));
    }

    @Operation(summary = "Get all exercises", description = "Returns a paginated list of all exercises, optionally filtered by muscle group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<ExerciseDTO>> getAllExercises(
            @Parameter(description = "Filter by muscle group") @RequestParam(required = false) MuscleGroup muscleGroup,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        if (muscleGroup != null) {
            return ResponseEntity.ok(exerciseService.getByMuscleGroup(muscleGroup, pageable));
        }
        return ResponseEntity.ok(exerciseService.getAll(pageable));
    }

    @Operation(summary = "Update exercise", description = "Fully replaces an exercise's data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise updated",
                    content = @Content(schema = @Schema(implementation = ExerciseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercise not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExerciseDTO> updateExercise(
            @Parameter(description = "Exercise UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody ExerciseDTO dto) {
        return ResponseEntity.ok(exerciseService.update(id, dto));
    }

    @Operation(summary = "Delete exercise", description = "Permanently removes an exercise from the catalogue")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise deleted"),
            @ApiResponse(responseCode = "404", description = "Exercise not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(
            @Parameter(description = "Exercise UUID", required = true) @PathVariable UUID id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
