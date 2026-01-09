package com.example.training_service.Controller;



import com.example.training_service.DTO.ExerciseDTO;
import com.example.training_service.DTO.TrainingDTO;
import com.example.training_service.Service.TrainingService;
import com.example.training_service.model.Exercise;
import com.example.training_service.model.Training;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(
            TrainingService trainingService
    ) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public ResponseEntity<Training> postTrainings(
            @Valid @RequestBody TrainingDTO dto
    ) {

        Training created = trainingService.createdTraining(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PostMapping("/{id}/exercise")
    public ResponseEntity<Exercise> createExercise(
            @Valid @RequestBody ExerciseDTO dto,
            @PathVariable UUID trainingId
    ) {
        Exercise exercise1 = trainingService.addExercises(dto,trainingId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(exercise1);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainings(
            TrainingDTO dto,
            @PathVariable UUID id
    ){
        trainingService.deleteTraining(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Training> getTrainings(
            @PathVariable UUID id
    ){
        trainingService.getTraining(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(trainingService.getTraining(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTrainings(
            @Valid @RequestBody TrainingDTO dto,
            @PathVariable UUID id
    ){

        trainingService.updateTraining(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(trainingService.getTraining(id));
    }
}