package com.example.training_service.dto;

import com.example.training_service.model.MuscleGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Exercise data transfer object")
public class ExerciseDTO {

    @Schema(description = "Unique exercise identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Exercise name must not be blank")
    @Size(min = 2, max = 128, message = "Exercise name must be between 2 and 128 characters")
    @Schema(description = "Exercise name", example = "Bench Press", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 1024, message = "Description must not exceed 1024 characters")
    @Schema(description = "Exercise description", example = "Lie on a flat bench and press the barbell upward")
    private String description;

    @NotNull(message = "Muscle group must not be null")
    @Schema(description = "Primary muscle group targeted", example = "CHEST", requiredMode = Schema.RequiredMode.REQUIRED)
    private MuscleGroup muscleGroup;

    @Schema(description = "UUID of the user who created this exercise", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID createdByUserId;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
