package com.example.training_service.repository;

import com.example.training_service.model.Exercise;
import com.example.training_service.model.MuscleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    Page<Exercise> findByMuscleGroup(MuscleGroup muscleGroup, Pageable pageable);
}
