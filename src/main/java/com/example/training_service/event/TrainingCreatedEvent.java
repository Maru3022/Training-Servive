package com.example.training_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingCreatedEvent {

    private UUID trainingId;
    private UUID userId;
    private LocalDate trainingDate;
    private String trainingName;
}
