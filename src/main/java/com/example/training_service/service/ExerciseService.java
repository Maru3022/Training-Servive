package com.example.training_service.service;

import com.example.training_service.dto.ExerciseDTO;
import com.example.training_service.exception.EntityNotFoundException;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.MuscleGroup;
import com.example.training_service.repository.ExerciseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Transactional
    public ExerciseDTO create(ExerciseDTO dto) {
        Exercise exercise = Exercise.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .muscleGroup(dto.getMuscleGroup())
                .createdByUserId(dto.getCreatedByUserId())
                .build();
        return toDTO(exerciseRepository.save(exercise));
    }

    @Transactional(readOnly = true)
    public ExerciseDTO getById(UUID id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ExerciseDTO> getAll(Pageable pageable) {
        return exerciseRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ExerciseDTO> getByMuscleGroup(MuscleGroup muscleGroup, Pageable pageable) {
        return exerciseRepository.findByMuscleGroup(muscleGroup, pageable).map(this::toDTO);
    }

    @Transactional
    public ExerciseDTO update(UUID id, ExerciseDTO dto) {
        Exercise exercise = findOrThrow(id);
        exercise.setName(dto.getName());
        exercise.setDescription(dto.getDescription());
        exercise.setMuscleGroup(dto.getMuscleGroup());
        exercise.setCreatedByUserId(dto.getCreatedByUserId());
        return toDTO(exerciseRepository.save(exercise));
    }

    @Transactional
    public void delete(UUID id) {
        if (!exerciseRepository.existsById(id)) {
            throw new EntityNotFoundException("Exercise", id);
        }
        exerciseRepository.deleteById(id);
    }

    private Exercise findOrThrow(UUID id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exercise", id));
    }

    public ExerciseDTO toDTO(Exercise exercise) {
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .muscleGroup(exercise.getMuscleGroup())
                .createdByUserId(exercise.getCreatedByUserId())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }
}
