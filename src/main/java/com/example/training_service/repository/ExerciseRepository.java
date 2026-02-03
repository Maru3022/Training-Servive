package com.example.training_service.repository;


import com.example.training_service.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

}
