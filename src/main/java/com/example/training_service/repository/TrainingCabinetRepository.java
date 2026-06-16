package com.example.training_service.repository;

import com.example.training_service.model.TrainingCabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingCabinetRepository extends JpaRepository<TrainingCabinet, UUID> {

    boolean existsByCorrelationId(String correlationId);

    boolean existsByUserId(UUID userId);

    Optional<TrainingCabinet> findByCorrelationId(String correlationId);

    Optional<TrainingCabinet> findByUserId(UUID userId);
}
