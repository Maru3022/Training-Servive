package com.example.training_service.Service;

import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Repository.ExerciseRepository;
import com.example.training_service.Repository.ExerciseSetRepository;
import com.example.training_service.Repository.TrainingRepository;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private ExerciseSetRepository exerciseSetRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    @DisplayName("Должен успешно создать тренировку из DTO")
    void createdTraining_Success() {
        // Given (Дано)
        TrainingDTO dto = new TrainingDTO(
                null,
                LocalDate.now(),
                UUID.randomUUID(),
                "Утренняя тренировка",
                "PLANNED",
                Collections.emptyList()
        );

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When (Когда)
        Training result = trainingService.createdTraining(dto);

        // Then (Тогда)
        assertNotNull(result);
        assertEquals("Утренняя тренировка", result.getTraining_name());
        assertEquals(TrainingStatus.PLANNED, result.getStatus());
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение, если тренировка не найдена")
    void getTraining_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(trainingRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> trainingService.getTraining(id));
    }
}