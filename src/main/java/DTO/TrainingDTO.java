package DTO;

import model.TrainingStatus;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record TrainingDTO(
        UUID id,
        @NotNull LocalDate data,
        @NotNull UUID userId,
        @NotNull TrainingStatus status
) {}