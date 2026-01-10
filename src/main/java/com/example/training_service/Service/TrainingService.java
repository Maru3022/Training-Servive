package com.example.training_service.Service;


import com.example.training_service.DTO.ExerciseDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Repository.ExerciseRepository;
import com.example.training_service.Repository.TrainingRepository;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import jakarta.persistence.EntityExistsException;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    public TrainingService(
            TrainingRepository trainingRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public Training createdTraining(
            TrainingDTO dto
    ) {

        log.info("Creating new training '{}' for user: {}", dto.training_name(), dto.userId());
        Training training = new Training();

        training.setId(UUID.randomUUID());
        training.setData(dto.data());
        training.setUserId(dto.userId());
        training.setTraining_name(dto.training_name());
        training.setStatus(TrainingStatus.valueOf(dto.status()));

        return trainingRepository.save(training);
    }

    public Training updateTraining(
            UUID id,
            TrainingDTO dto
    ) {
        log.info("Updating training '{}' for user: {}", dto.training_name(), dto.userId());

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        training.setData(dto.data());
        training.setUserId(dto.userId());

        return trainingRepository.save(training);
    }

    public void deleteTraining(
            UUID id
    ) {
        log.info("Deleting training for user: {}", id);

        if (!trainingRepository.existsById(id)) {
            throw new RuntimeException("Training don't delete");
        }
        trainingRepository.deleteById(id);
    }

    public Training getTraining(
            UUID id) {
        log.info("Getting training for user: {}", id);

        if (!trainingRepository.existsById(id)) {
            throw new RuntimeException("Training not found");
        }

        return trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));
    }

    public Exercise addExercise(
            UUID id,
            ExerciseDTO dto
    ) {

        log.info("Adding exercise '{}' for training_id: {}", dto.name_exercise(), dto.trainingId());
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new EntityExistsException("Training not found"));

        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setName_exercise(dto.name_exercise());
        exercise.setNotes(dto.notes());

        return exerciseRepository.save(exercise);
    }

    public Exercise getExercise(
            UUID id,
            ExerciseDTO dto
    ) {
        log.info("Getting exercise for training_id: {}", dto.trainingId());

        return exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityExistsException("Exercise not found"));
    }

    public Exercise updateExercise(
            UUID id,
            ExerciseDTO dto
    ) {
        log.info("Updating exercise '{}' for training_id:{}", dto.name_exercise(), dto.trainingId());

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityExistsException("Exercise not found"));

        exercise.setName_exercise(dto.name_exercise());
        exercise.setNotes(dto.notes());

        if (dto.trainingId() != null) {
            Training training = trainingRepository.findById(id)
                    .orElseThrow(() -> new EntityExistsException("Training not found"));
            exercise.setTraining(training);
        }

        return exerciseRepository.save(exercise);
    }

    public void deleteExercise(
            UUID id
    ) {
        log.info("Deleting exercise for training_id: {}", id);

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityExistsException("Exercise not found"));

        exerciseRepository.deleteById(id);
    }
}

