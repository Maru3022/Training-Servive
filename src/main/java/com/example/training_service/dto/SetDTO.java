package com.example.training_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload describing a single exercise set inside a training session.")
public class SetDTO {

    @Schema(description = "Set identifier. Optional for create operations.", example = "770e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull
    @Schema(description = "Exercise identifier the set belongs to.", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID exercise_id;

    @Min(0)
    @Schema(description = "Weight used for the set.", example = "80")
    private Integer weight;

    @Min(1)
    @Schema(description = "Number of repetitions completed.", example = "8")
    private Integer reps;

    @Min(1)
    @Schema(description = "Order of the set within the exercise.", example = "1")
    private Integer order_;
}
