package com.example.training_service.service;

import com.example.training_service.dto.ExerciseDTO;
import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.repository.ExerciseRepository;
import com.example.training_service.repository.ExerciseSetRepository;
import com.example.training_service.repository.TrainingRepository;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @DisplayName("Создание тренировки - проверка маппинга для всех уровней (DTO -> Entity)")
    void createdTraining_FullSuccess(){
        SetDTO setDTO = new SetDTO(UUID.randomUUID(),UUID.randomUUID(),100,10,1);
        ExerciseDTO exDTO = new ExerciseDTO(UUID.randomUUID(),UUID.randomUUID(),"Жим","Заметка", List.of(setDTO));
        TrainingDTO dto = new TrainingDTO(null, LocalDate.now(), UUID.randomUUID(), "Утренняя", "PLANNED", List.of(exDTO));

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(i -> i.getArgument(0));

        Training result = trainingService.createdTraining(dto);
        assertNotNull(result);
        assertEquals("Утренняя", result.getTraining_name());
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение, если тренировка не найдена")
    void getTraining_NotFound(){
        UUID id = UUID.randomUUID();
        when(trainingRepository.findById(id))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> trainingService.getTraining(id));
        assertEquals("Cannot delete: Training not found", ex.getMessage());
    }

    @Test
    @DisplayName("Патч сета - должен обновить только вес и повторения")
    void patchSetPerformance_Success(){
        UUID setId = UUID.randomUUID();

        ExerciseSet existingSet = new ExerciseSet();
        existingSet.setWeight(50);

        SetDTO patchDTO = new SetDTO(setId,UUID.randomUUID(), 90, 15,1);
        when(exerciseSetRepository.findById(setId))
                .thenReturn(Optional.of(existingSet));

        when(exerciseSetRepository.save(any(ExerciseSet.class)))
                .thenAnswer(i -> i.getArgument(0));

        ExerciseSet result = trainingService.patchSetPerformance(setId,patchDTO);
        assertEquals(90, result.getWeight());
        assertEquals(15, result.getReps());
    }

    @Test
    @DisplayName("Удаление тренировки - ошибка, если ID не существует")
    void deleteTraining_NotFound() {
        UUID id = UUID.randomUUID();
        when(trainingRepository.existsById(id))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> trainingService.deleteTraining(id));
        assertEquals("Training don't delete", ex.getMessage());
    }
}