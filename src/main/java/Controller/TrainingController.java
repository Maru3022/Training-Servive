package Controller;

import DTO.ExerciseDTO;
import DTO.TrainingDTO;
import Service.TrainingService;
import model.Exercise;
import model.Training;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final Exercise exercise;
    private final ExerciseDTO exerciseDTO;

    public TrainingController(
            TrainingService trainingService,
            Exercise exercise,
            ExerciseDTO exerciseDTO
    ) {
        this.trainingService = trainingService;
        this.exercise = exercise;
        this.exerciseDTO = exerciseDTO;
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

    //ToDo: Finish this method
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
