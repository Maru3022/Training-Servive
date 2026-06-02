package com.example.training_service.event;

import com.example.training_service.model.TrainingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingStatusChangedEvent {

    private UUID trainingId;
    private TrainingStatus oldStatus;
    private TrainingStatus newStatus;
    private LocalDateTime changedAt;
}
