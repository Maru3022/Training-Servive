package model;

import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class Training {

    @Id
    private UUID id;
    private LocalDate data;
    private UUID userId;
    private TrainingStatus status;

}
