package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_cabinets", indexes = {
        @Index(name = "idx_training_cabinet_correlation_id", columnList = "correlation_id", unique = true),
        @Index(name = "idx_training_cabinet_user_id", columnList = "user_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingCabinet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 128)
    private String correlationId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrainingCabinetStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
