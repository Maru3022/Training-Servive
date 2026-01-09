package com.example.training_service.Service;


import com.example.training_service.DTO.ExerciseDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Repository.ExerciseRepository;
import com.example.training_service.Repository.TrainingRepository;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.Training;
import jakarta.persistence.EntityExistsException;

import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;

    public TrainingService(
            TrainingRepository trainingRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.trainingRepository = trainingRepository;

        this.exerciseRepository = exerciseRepository;
    }

    public Training createdTraining(
            TrainingDTO dto
    ) {
        Training training = new Training();

         training.setId(UUID.randomUUID());
         training.setData(dto.data());
         training.setUserId(dto.userId());
         training.setTraining_name(dto.training_name());

        return trainingRepository.save(training);
    }

    public Exercise addExercises(
            ExerciseDTO dto,
            UUID trainingId
    ) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityExistsException("Training not found"));

        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setName_exercise(dto.name_exercise());
        exercise.setNotes(dto.notes());

        return exerciseRepository.save(exercise);
    }

    public Training updateTraining(
            UUID id,
            TrainingDTO dto
    ){
        Training training = trainingRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Training not found"));

        training.setData(dto.data());
        training.setUserId(dto.userId());

        return trainingRepository.save(training);
    }

    public void deleteTraining(
            UUID id
            ){
        if(!trainingRepository.existsById(id)){
            throw new RuntimeException("Training don't delete");
        }
        trainingRepository.deleteById(id);
    }

    public Training getTraining(
            UUID id){
        if(!trainingRepository.existsById(id)){
            throw new RuntimeException("Training not found");
        }

        return trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));
    }
}

