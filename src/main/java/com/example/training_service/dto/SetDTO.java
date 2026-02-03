package com.example.training_service.dto;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetDTO(
        UUID id,
        @NotNull UUID exercise_id,
        @NotNull int weight,
        @NotNull int reps,
        @NotNull int order
){}
