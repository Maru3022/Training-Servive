package com.example.training_service.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;
public record ExerciseDTO(
        UUID id,
        @NotNull UUID trainingId,
        @NotNull String name_exercise,
        @NotNull String notes
){}