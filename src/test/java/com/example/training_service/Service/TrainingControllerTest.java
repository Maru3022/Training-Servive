package com.example.training_service.Service;

import com.example.training_service.Controller.TrainingController;
import com.example.training_service.TrainingBulkLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private TrainingBulkLoader bulkLoader;

    @Test
    void triggerBulkLoad_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/trainings/bulk-load")
                        .param("count", "100")
                        .param("batchSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bulk loading started in background..."));
    }

    @Test
    void postTrainings_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Отправляем пустой JSON, а у нас там @Valid и @NotNull в DTO
        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}