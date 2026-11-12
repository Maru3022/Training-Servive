package com.example.training_service.service;

import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.Training;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainingCreationTest {
    @Configuration
    @EnableAsync
    static class AsyncConfiguration {}

    @Test
    void creationReturnsPersistedIdWithAsyncInfrastructureEnabled() {
        var repository = mock(TrainingRepository.class);
        UUID id = UUID.randomUUID();
        when(repository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(id);
            return training;
        });
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(AsyncConfiguration.class);
            context.registerBean(TrainingService.class, () -> new TrainingService(
                    repository, mock(ExerciseSetRepository.class), Optional.empty()));
            context.refresh();
            assertEquals(id, context.getBean(TrainingService.class)
                    .createTraining(TrainingDTO.builder().build()));
            verify(repository).save(any(Training.class));
        }
    }
}
