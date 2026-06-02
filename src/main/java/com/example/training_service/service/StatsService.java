package com.example.training_service.service;

import com.example.training_service.dto.UserStatsDTO;
import com.example.training_service.exception.EntityNotFoundException;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.MuscleGroup;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import com.example.training_service.repository.ExerciseRepository;
import com.example.training_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final TrainingService trainingService;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public StatsService(TrainingService trainingService,
                        ExerciseRepository exerciseRepository,
                        UserRepository userRepository) {
        this.trainingService = trainingService;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserStatsDTO computeStats(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User", userId);
        }

        List<Training> trainings = trainingService.getRawTrainingsByUser(userId);

        long totalTrainings = trainings.size();
        long completedTrainings = trainings.stream()
                .filter(t -> TrainingStatus.COMPLETED.equals(t.getTraining_status()))
                .count();
        long cancelledTrainings = trainings.stream()
                .filter(t -> TrainingStatus.CANCELLED.equals(t.getTraining_status()))
                .count();

        List<ExerciseSet> allSets = trainings.stream()
                .flatMap(t -> t.getSets().stream())
                .collect(Collectors.toList());

        long totalSets = allSets.size();
        long totalReps = allSets.stream()
                .mapToLong(s -> s.getReps() != null ? s.getReps() : 0L)
                .sum();
        double totalWeightLifted = allSets.stream()
                .mapToDouble(s -> {
                    double weight = s.getWeight() != null ? s.getWeight() : 0.0;
                    double reps = s.getReps() != null ? s.getReps() : 0.0;
                    return weight * reps;
                })
                .sum();

        double averageSetsPerTraining = completedTrainings > 0
                ? (double) totalSets / completedTrainings
                : 0.0;

        MuscleGroup mostUsedMuscleGroup = computeMostUsedMuscleGroup(allSets);

        return UserStatsDTO.builder()
                .totalTrainings(totalTrainings)
                .completedTrainings(completedTrainings)
                .cancelledTrainings(cancelledTrainings)
                .totalSets(totalSets)
                .totalReps(totalReps)
                .totalWeightLifted(totalWeightLifted)
                .mostUsedMuscleGroup(mostUsedMuscleGroup)
                .averageSetsPerTraining(averageSetsPerTraining)
                .build();
    }

    private MuscleGroup computeMostUsedMuscleGroup(List<ExerciseSet> sets) {
        List<UUID> exerciseIds = sets.stream()
                .map(ExerciseSet::getExercise_id)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (exerciseIds.isEmpty()) {
            return null;
        }

        Map<MuscleGroup, Long> muscleGroupCounts = exerciseIds.stream()
                .map(id -> exerciseRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Exercise::getMuscleGroup, Collectors.counting()));

        return muscleGroupCounts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
