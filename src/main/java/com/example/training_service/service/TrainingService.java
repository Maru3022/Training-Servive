package com.example.training_service.service;

import com.example.training_service.dto.SetDTO;
import com.example.training_service.dto.TrainingDTO;
import com.example.training_service.model.ExerciseSet;
import com.example.training_service.model.Training;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrainingService {

    public UUID createdTrainingAsync(TrainingDTO trainingDTO) {
        // Async creation logic placeholder
        return UUID.randomUUID();
    }

    public Training getTraining(UUID id) {
        // Get training logic placeholder
        Training training = new Training();
        training.setId(id);
        return training;
    }

    public Training updateFullTraining(UUID id, TrainingDTO trainingDTO) {
        // Update training logic placeholder
        Training training = new Training();
        training.setId(id);
        training.setTraining_name(trainingDTO.getTraining_name());
        return training;
    }

    public void deleteTraining(UUID id) {
        // Delete training logic placeholder
    }

    public ExerciseSet patchSetPerformance(UUID setId, SetDTO setDTO) {
        // Patch set logic placeholder
        ExerciseSet exerciseSet = new ExerciseSet();
        exerciseSet.setId(setId);
        exerciseSet.setWeight(setDTO.getWeight());
        return exerciseSet;
    }

    public void deleteSpecificTraining(UUID exerciseId) {
        // Delete specific training logic placeholder
    }
}
