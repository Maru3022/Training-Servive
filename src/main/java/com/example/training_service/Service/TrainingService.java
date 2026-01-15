package com.example.training_service.Service;


import com.example.training_service.DTO.SetDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Repository.ExerciseRepository;
import com.example.training_service.Repository.ExerciseSetRepository;
import com.example.training_service.Repository.TrainingRepository;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final ExerciseRepository exerciseRepository;

    private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public TrainingService(
            TrainingRepository trainingRepository,
            ExerciseRepository exerciseRepository,
            ExerciseSetRepository exerciseSetRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseSetRepository = exerciseSetRepository;
    }

    public Training createdTraining(
            TrainingDTO dto
    ) {

        log.info("Creating new training '{}' for user: {}", dto.training_name(), dto.userId());
        Training training = new Training();
        training.setId(UUID.randomUUID());

        mapDtoToEntity(dto, training);
        return trainingRepository.save(training);
    }

    public Training updateFullTraining(
            UUID id,
            TrainingDTO training_dto
    ) {
        log.info("Updating training '{}' for user: {}", training_dto.training_name(), training_dto.userId());

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        training.getExercises().clear();

        mapDtoToEntity(training_dto, training);
        return trainingRepository.save(training);
    }

    public ExerciseSet patchSetPerformance(
            UUID set_id,
            SetDTO setDTO
    ) {
        log.info("Patching set ID: {}. New weight: {}, reps: {}", setDTO.id(), setDTO.weight(), setDTO.reps());

        ExerciseSet set = exerciseSetRepository.findById(set_id)
                .orElseThrow(() -> new EntityExistsException("Exercise set not found"));

        set.setWeight(setDTO.weight());
        set.setReps(setDTO.reps());

        return exerciseSetRepository.save(set);
    }

    public void deleteSpecificTraining(
            UUID exercise_id
    ) {
        log.info("Deleting specific exercise '{}'", exercise_id);

        if (!exerciseRepository.existsById(exercise_id)) {
            throw new EntityNotFoundException("Exercise not found");
        }
        exerciseRepository.deleteById(exercise_id);
    }

    public void deleteTraining(
            UUID id
    ) {
        log.info("Deleting training and all its contents for id: {}", id);
        if (!trainingRepository.existsById(id)) {
            throw new RuntimeException("Training don't delete");
        }
        trainingRepository.deleteById(id);
    }

    public Training getTraining(
            UUID id) {
        log.info("Getting full training profile for id: {}", id);

        return trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot delete: Training not found"));
    }

    private void mapDtoToEntity(
            TrainingDTO training_dto,
            Training training
    ) {
        training.setData(training_dto.data());
        training.setUserId(training_dto.userId());
        training.setTraining_name(training_dto.training_name());
        training.setStatus(TrainingStatus.valueOf(training_dto.status()));

        if (training_dto.exercises() != null && !training_dto.exercises().isEmpty()) {
            List<Exercise> exercises = training_dto.exercises().stream().map(
                    exDTO -> {
                        Exercise exercise = new Exercise();

                        exercise.setId(UUID.randomUUID());
                        exercise.setName_exercise(exDTO.name_exercise());
                        exercise.setNotes(exDTO.notes());

                        exercise.setTraining(training);

                        if (exDTO.sets() != null && !exDTO.sets().isEmpty()) {
                            List<ExerciseSet> sets = exDTO.sets().stream().map(
                                    setDTO -> {
                                        ExerciseSet set = new ExerciseSet();

                                        set.setId(UUID.randomUUID());
                                        set.setWeight(setDTO.weight());
                                        set.setReps(setDTO.reps());
                                        set.setOrder(setDTO.order());

                                        set.setExercise(exercise);
                                        return set;
                                    }).toList();

                            exercise.setSets(sets);
                        }
                        return exercise;
                    }).toList();

            training.setExercises(exercises);
        }
    }
}

