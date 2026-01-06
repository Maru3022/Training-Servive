package model;

import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.UUID;

public class Training {

    @Id
    private UUID id;
    private LocalDate data;
    private UUID userId;
    private TrainingStatus status;

}
