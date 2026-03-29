package com.example.training_service.service;

import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.repository.ExerciseRepository;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import com.example.training_service.repository.OutboxRepository;
import com.example.training_service.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@CacheConfig(cacheNames = "trainings")
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final OutboxRepository outboxRepository; // New repository
    private final ObjectMapper objectMapper; // For JSON serialization

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TrainingService(
            TrainingRepository trainingRepository,
            ExerciseRepository exerciseRepository,
            ExerciseSetRepository exerciseSetRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.trainingRepository = trainingRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public UUID createdTrainingAsync(TrainingDTO dto) {
        UUID id = UUID.randomUUID();

        TrainingDTO eventsDto = new TrainingDTO(
                id,
                dto.data(),
                dto.userId(),
                dto.training_name(),
                dto.status(),
                dto.exercises()
        );

        // OUTBOX IMPLEMENTATION: Save event instead of sending to Kafka
        saveToOutbox(id, "TRAINING_CREATED_ASYNC", eventsDto);

        log.debug("Training request accepted via Outbox: {}", id);
        return id;
    }

    @CachePut(key = "#result.id")
    @Transactional
    public Training createdTraining(TrainingDTO dto) {
        log.info("Creating new training '{}' for user: {}", dto.training_name(), dto.userId());

        Training training = new Training();
        UUID trainingId = UUID.randomUUID();
        training.setId(trainingId);

        mapDtoToEntity(dto, training);
        Training savedTraining = trainingRepository.save(training);
        log.info("Training saved to DB with ID: {}", trainingId);

        // OUTBOX IMPLEMENTATION: Save event in the same transaction
        saveToOutbox(trainingId, "TRAINING_CREATED", dto);

        return savedTraining;
    }

    @CachePut(key = "#id")
    @Transactional
    public Training updateFullTraining(UUID id, TrainingDTO training_dto) {
        log.info("Updating training '{}' for user: {}", training_dto.training_name(), training_dto.userId());

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        training.getExercises().clear();
        mapDtoToEntity(training_dto, training);

        Training updatedTraining = trainingRepository.save(training);
        log.info("Training updated in DB. ID: {}", id);

        // OUTBOX IMPLEMENTATION: Save event for update
        saveToOutbox(id, "TRAINING_UPDATED", training_dto);

        return updatedTraining;
    }

    private void saveToOutbox(UUID aggregateId, String type, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .eventType(type)
                    .payload(jsonPayload)
                    .status(OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Outbox record saved for {} event", type);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Outbox payload: {}", e.getMessage());
            throw new RuntimeException("Outbox error: serialization failed");
        }
    }


    @Transactional
    public void saveBatch(List<TrainingDTO> dtos) {
        List<Training> trainings = dtos.stream().map(dto -> {
            Training training = new Training();
            training.setId(dto.id());
            mapDtoToEntity(dto, training);
            return training;
        }).toList();

        trainingRepository.saveAll(trainings);
        log.info("Successfully saved batch of {} trainings", trainings.size());
    }

    public ExerciseSet patchSetPerformance(UUID set_id, SetDTO setDTO) {
        log.info("Patching set ID: {}. New weight: {}, reps: {}", set_id, setDTO.weight(), setDTO.reps());

        ExerciseSet set = exerciseSetRepository.findById(set_id)
                .orElseThrow(() -> new EntityExistsException("Exercise set not found"));

        set.setWeight(setDTO.weight());
        set.setReps(setDTO.reps());

        return exerciseSetRepository.save(set);
    }

    public void deleteSpecificTraining(UUID exercise_id) {
        log.info("Deleting specific exercise '{}'", exercise_id);

        if (!exerciseRepository.existsById(exercise_id)) {
            throw new EntityNotFoundException("Exercise not found");
        }
        exerciseRepository.deleteById(exercise_id);
    }

    @CacheEvict(key = "#id")
    public void deleteTraining(UUID id) {
        log.info("Deleting training and all its contents for id: {}", id);
        if (!trainingRepository.existsById(id)) {
            throw new RuntimeException("Training don't delete");
        }
        trainingRepository.deleteById(id);
    }

    @Cacheable(value = "trainings", key = "#id")
    public Training getTraining(UUID id) {
        log.info("Getting full training profile for id: {}", id);
        return trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));
    }

    private void mapDtoToEntity(TrainingDTO training_dto, Training training) {
        training.setData(training_dto.data());
        training.setUserId(training_dto.userId());
        training.setTraining_name(training_dto.training_name());

        if (training_dto.status() != null) {
            try {
                training.setStatus(TrainingStatus.valueOf(training_dto.status()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown status '{}', defaulting to PLANNED", training_dto.status());
                training.setStatus(TrainingStatus.PLANNED);
            }
        } else {
            training.setStatus(TrainingStatus.PLANNED);
        }

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