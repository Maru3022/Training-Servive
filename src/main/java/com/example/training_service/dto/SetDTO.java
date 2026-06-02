package com.example.training_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Exercise set data transfer object")
public class SetDTO {

    @Schema(description = "Unique set identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull(message = "Exercise ID must not be null")
    @Schema(description = "Reference to the exercise performed", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID exercise_id;

    @Min(value = 0, message = "Weight must be non-negative")
    @Schema(description = "Weight used in kilograms", example = "80")
    private Integer weight;

    @Min(value = 1, message = "Reps must be at least 1")
    @Schema(description = "Number of repetitions performed", example = "10")
    private Integer reps;

    @Min(value = 1, message = "Order must be at least 1")
    @Schema(description = "Order of this set within the training", example = "1")
    private Integer order_;
}
