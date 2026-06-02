package com.example.training_service.dto;

import com.example.training_service.model.TrainingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Training session data transfer object")
public class TrainingDTO {

    @Schema(description = "Unique training identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull(message = "Training date must not be null")
    @Schema(description = "Date of the training session", example = "2025-06-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate training_date;

    @NotNull(message = "User ID must not be null")
    @Schema(description = "ID of the user who owns this training", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID user_id;

    @NotBlank(message = "Training name must not be blank")
    @Size(min = 1, max = 128, message = "Training name must be between 1 and 128 characters")
    @Schema(description = "Name of the training session", example = "Morning Push Day", requiredMode = Schema.RequiredMode.REQUIRED)
    private String training_name;

    @NotNull(message = "Training status must not be null")
    @Schema(description = "Current status of the training session", example = "PLANNED", requiredMode = Schema.RequiredMode.REQUIRED)
    private TrainingStatus training_status;

    @Valid
    @Schema(description = "List of exercise sets in this training session")
    private List<SetDTO> sets;

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
