package com.example.training_service.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CabinetResponseEvent {
    private String correlationId;
    private UUID userId;
    private UUID cabinetId;
    private boolean success;
    private String errorMessage;
}
