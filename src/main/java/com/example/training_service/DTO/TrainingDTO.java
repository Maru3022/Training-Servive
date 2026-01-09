package com.example.training_service.DTO;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record TrainingDTO(
        UUID id,
        @NotNull LocalDate data,
        @NotNull UUID userId,
        @NotNull String training_name,
        @NotNull String status
) {}