package com.example.training_service.dto;

import com.example.training_service.model.MuscleGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Aggregated training statistics for a user")
public class UserStatsDTO {

    @Schema(description = "Total number of training sessions", example = "42")
    private long totalTrainings;

    @Schema(description = "Number of completed training sessions", example = "35")
    private long completedTrainings;

    @Schema(description = "Number of cancelled training sessions", example = "3")
    private long cancelledTrainings;

    @Schema(description = "Total number of sets performed across all trainings", example = "420")
    private long totalSets;

    @Schema(description = "Total number of repetitions performed", example = "3780")
    private long totalReps;

    @Schema(description = "Total weight lifted in kilograms", example = "25400.0")
    private double totalWeightLifted;

    @Schema(description = "Most frequently targeted muscle group", example = "CHEST")
    private MuscleGroup mostUsedMuscleGroup;

    @Schema(description = "Average number of sets per completed training", example = "12.0")
    private double averageSetsPerTraining;
}
