package com.example.training_service.repository;


import com.example.training_service.model.ExerciseSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, UUID> {
}
