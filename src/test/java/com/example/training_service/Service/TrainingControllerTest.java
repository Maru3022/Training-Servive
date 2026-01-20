package com.example.training_service.Service;

import com.example.training_service.Controller.TrainingController;
import com.example.training_service.DTO.SetDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.TrainingBulkLoader;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingBulkLoader trainingBulkLoader;

    @MockBean
    private TrainingService trainingService;

    @Autowired
    private ObjectMapper objectMapper;

    private TrainingDTO defaultDto;
    private UUID trainingId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        trainingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        defaultDto = new TrainingDTO(
                null,
                LocalDate.now(),
                userId,
                "Leg Day",
                "PLANNED",
                List.of()
        );
    }

    @Test
    void triggerBulkLoad_ShouldReturnOk() throws Exception {
        mockMvc.perform(
                        post("/trainings/bulk-load")
                                .param("count", "100")
                                .param("batchSize", "10")
                )
                .andExpect(status().isOk());

        verify(trainingBulkLoader, timeout(100)).runBulkLoad(100, 10);
    }

    @Test
    void postTrainings_ShouldReturnAccepted() throws Exception {
        // Исправлено: мокаем асинхронный метод и возвращаем UUID
        when(trainingService.createdTrainingAsync(any(TrainingDTO.class))).thenReturn(trainingId);

        mockMvc.perform(
                        post("/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(defaultDto)))
                .andExpect(status().isAccepted()) // Статус 202
                .andExpect(content().string(containsString(trainingId.toString()))); // Проверка ID в теле
    }

    @Test
    void postTrainings_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        mockMvc.perform(
                        post("/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"invalid\": \"json\"}")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTraining_ShouldReturnTraining() throws Exception {
        Training training = new Training();
        training.setId(trainingId);
        training.setTraining_name("Morning Run");

        when(trainingService.getTraining(trainingId)).thenReturn(training);

        mockMvc.perform(
                        get("/trainings/{id}", trainingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainingId.toString()))
                .andExpect(jsonPath("$.training_name").value("Morning Run"));
    }

    @Test
    void updateTraining_ShouldReturnOk() throws Exception {
        TrainingDTO trainingDto = new TrainingDTO(trainingId, LocalDate.now(),
                userId, "Update Name", "PLANNED", List.of());

        Training updatedTraining = new Training();
        updatedTraining.setId(trainingId);
        updatedTraining.setTraining_name("Update Name");

        when(trainingService.updateFullTraining(eq(trainingId), any(TrainingDTO.class))).thenReturn(updatedTraining);

        mockMvc.perform(
                        put("/trainings/{id}", trainingId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(trainingDto))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.training_name").value("Update Name"));
    }

    @Test
    void deleteTrainings_ShouldReturnNoContent() throws Exception {
        doNothing().when(trainingService).deleteTraining(trainingId);

        mockMvc.perform(
                delete("/trainings/{id}", trainingId)
        ).andExpect(status().isNoContent());

        verify(trainingService).deleteTraining(trainingId);
    }

    @Test
    void patchSet_ShouldReturnOk() throws Exception {
        UUID setId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        SetDTO setDTO = new SetDTO(setId, exerciseId, 50, 12, 1);

        ExerciseSet patchedSet = new ExerciseSet();
        patchedSet.setId(setId);
        patchedSet.setWeight(50);

        when(trainingService.patchSetPerformance(eq(setId), any(SetDTO.class)))
                .thenReturn(patchedSet);

        mockMvc.perform(
                        patch("/trainings/sets/{setId}", setId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(setDTO))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(setId.toString()))
                .andExpect(jsonPath("$.weight").value(50));
    }

    @Test
    void deleteExercise_ShouldReturnNoContent() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        doNothing().when(trainingService).deleteSpecificTraining(exerciseId);

        mockMvc.perform(
                delete("/trainings/exercises/{exerciseId}", exerciseId)
        ).andExpect(status().isNoContent());

        verify(trainingService).deleteSpecificTraining(exerciseId);
    }
}