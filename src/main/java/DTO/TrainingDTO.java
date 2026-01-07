package DTO;

import jakarta.validation.constraints.NotNull;
import model.TrainingStatus;
import java.time.LocalDate;
import java.util.UUID;

public record TrainingDTO(
        UUID id,
        @NotNull LocalDate data,
        @NotNull UUID userId,
        @NotNull TrainingStatus status,
        @NotNull String training_name
) {}