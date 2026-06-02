package com.example.training_service.service;

import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.event.TrainingCreatedEvent;
import com.example.training_service.event.TrainingStatusChangedEvent;
import com.example.training_service.exception.EntityNotFoundException;
import com.example.training_service.kafka.TrainingEventProducer;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import com.example.training_service.specification.TrainingSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingRepository trainingRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final Optional<TrainingEventProducer> eventProducer;

    public TrainingService(TrainingRepository trainingRepository,
                           ExerciseSetRepository exerciseSetRepository,
                           Optional<TrainingEventProducer> eventProducer) {
        this.trainingRepository = trainingRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.eventProducer = eventProducer;
    }

    @Async
    @Transactional
    public UUID createdTrainingAsync(TrainingDTO dto) {
        Training training = Training.builder()
                .training_name(dto.getTraining_name())
                .training_date(dto.getTraining_date())
                .user_id(dto.getUser_id())
                .training_status(dto.getTraining_status())
                .sets(new ArrayList<>())
                .build();

        if (dto.getSets() != null) {
            for (SetDTO setDTO : dto.getSets()) {
                ExerciseSet set = ExerciseSet.builder()
                        .exercise_id(setDTO.getExercise_id())
                        .weight(setDTO.getWeight())
                        .reps(setDTO.getReps())
                        .order_(setDTO.getOrder_())
                        .training(training)
                        .build();
                training.getSets().add(set);
            }
        }

        Training saved = trainingRepository.save(training);

        try {
            eventProducer.ifPresent(p -> p.sendTrainingCreated(TrainingCreatedEvent.builder()
                    .trainingId(saved.getId())
                    .userId(saved.getUser_id())
                    .trainingDate(saved.getTraining_date())
                    .trainingName(saved.getTraining_name())
                    .build()));
        } catch (Exception ex) {
            log.error("Failed to publish TrainingCreatedEvent for trainingId={}: {}",
                    saved.getId(), ex.getMessage(), ex);
        }

        return saved.getId();
    }

    @Cacheable(value = "trainings", key = "#id")
    @Transactional(readOnly = true)
    public TrainingDTO getTraining(UUID id) {
        return toDTO(findOrThrow(id));
    }

    @CacheEvict(value = "trainings", key = "#id")
    @Transactional
    public TrainingDTO updateFullTraining(UUID id, TrainingDTO dto) {
        Training training = findOrThrow(id);

        TrainingStatus oldStatus = training.getTraining_status();

        training.setTraining_name(dto.getTraining_name());
        training.setTraining_date(dto.getTraining_date());
        training.setUser_id(dto.getUser_id());
        training.setTraining_status(dto.getTraining_status());

        training.getSets().clear();
        if (dto.getSets() != null) {
            for (SetDTO setDTO : dto.getSets()) {
                ExerciseSet set = ExerciseSet.builder()
                        .exercise_id(setDTO.getExercise_id())
                        .weight(setDTO.getWeight())
                        .reps(setDTO.getReps())
                        .order_(setDTO.getOrder_())
                        .training(training)
                        .build();
                training.getSets().add(set);
            }
        }

        Training saved = trainingRepository.save(training);

        if (oldStatus != dto.getTraining_status()) {
            try {
                eventProducer.ifPresent(p -> p.sendTrainingStatusChanged(TrainingStatusChangedEvent.builder()
                        .trainingId(saved.getId())
                        .oldStatus(oldStatus)
                        .newStatus(saved.getTraining_status())
                        .changedAt(LocalDateTime.now())
                        .build()));
            } catch (Exception ex) {
                log.error("Failed to publish TrainingStatusChangedEvent for trainingId={}: {}",
                        saved.getId(), ex.getMessage(), ex);
            }
        }

        return toDTO(saved);
    }

    @CacheEvict(value = "trainings", key = "#id")
    @Transactional
    public void deleteTraining(UUID id) {
        if (!trainingRepository.existsById(id)) {
            throw new EntityNotFoundException("Training", id);
        }
        trainingRepository.deleteById(id);
    }

    @Transactional
    public SetDTO patchSetPerformance(UUID setId, SetDTO dto) {
        ExerciseSet set = exerciseSetRepository.findById(setId)
                .orElseThrow(() -> new EntityNotFoundException("ExerciseSet", setId));

        if (dto.getWeight() != null) {
            set.setWeight(dto.getWeight());
        }
        if (dto.getReps() != null) {
            set.setReps(dto.getReps());
        }
        if (dto.getOrder_() != null) {
            set.setOrder_(dto.getOrder_());
        }

        ExerciseSet saved = exerciseSetRepository.save(set);
        return toSetDTO(saved);
    }

    @Transactional
    public void deleteSpecificTraining(UUID exerciseId) {
        ExerciseSet set = exerciseSetRepository.findById(exerciseId)
                .orElseThrow(() -> new EntityNotFoundException("ExerciseSet", exerciseId));
        exerciseSetRepository.delete(set);
    }

    @Transactional(readOnly = true)
    public Page<TrainingDTO> getFilteredTrainings(UUID userId, TrainingStatus status,
                                                   LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Training> spec = TrainingSpecification.filter(userId, status, from, to);
        return trainingRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<TrainingDTO> getTrainingsByUser(UUID userId, Pageable pageable) {
        return trainingRepository.findByUser_id(userId, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<Training> getRawTrainingsByUser(UUID userId) {
        return trainingRepository.findByUser_id(userId);
    }

    private Training findOrThrow(UUID id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training", id));
    }

    public TrainingDTO toDTO(Training training) {
        List<SetDTO> sets = training.getSets().stream()
                .map(this::toSetDTO)
                .collect(Collectors.toList());

        return TrainingDTO.builder()
                .id(training.getId())
                .training_name(training.getTraining_name())
                .training_date(training.getTraining_date())
                .user_id(training.getUser_id())
                .training_status(training.getTraining_status())
                .sets(sets)
                .createdAt(training.getCreatedAt())
                .updatedAt(training.getUpdatedAt())
                .build();
    }

    private SetDTO toSetDTO(ExerciseSet set) {
        return SetDTO.builder()
                .id(set.getId())
                .exercise_id(set.getExercise_id())
                .weight(set.getWeight())
                .reps(set.getReps())
                .order_(set.getOrder_())
                .build();
    }
}
