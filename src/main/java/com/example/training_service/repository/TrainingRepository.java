package com.example.training_service.repository;



import com.example.training_service.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingRepository extends JpaRepository<Training, UUID> {
}
