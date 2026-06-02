package com.example.training_service.repository;

import com.example.training_service.model.ExerciseSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, UUID> {
}
