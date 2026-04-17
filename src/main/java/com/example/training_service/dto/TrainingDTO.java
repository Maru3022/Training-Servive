package com.example.training_service.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload used to create or update a training session.")
public class TrainingDTO {

    @Schema(description = "Training identifier. Optional when creating a training.", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;

    @NotNull
    @Schema(description = "Date of the training session.", example = "2026-04-17", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate training_date;

    @NotNull
    @Schema(description = "Owner of the training plan.", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID user_id;

    @NotBlank
    @Schema(description = "Human-friendly training title.", example = "Strength Block A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String training_name;

    @NotBlank
    @Schema(description = "Lifecycle state for the training.", example = "PLANNED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String training_status;

    @Valid
    @NotNull
    @ArraySchema(schema = @Schema(implementation = SetDTO.class), arraySchema = @Schema(description = "Ordered list of exercise sets in the training.", requiredMode = Schema.RequiredMode.REQUIRED))
    private List<SetDTO> sets;
}
