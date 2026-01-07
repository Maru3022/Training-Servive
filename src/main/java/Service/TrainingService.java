package Service;

import DTO.ExerciseDTO;
import DTO.TrainingDTO;
import Repository.ExerciseRepository;
import Repository.TrainingRepository;
import model.Exercise;
import model.Training;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseDTO exerciseDTO;

    public TrainingService(
            TrainingRepository trainingRepository,
            ExerciseDTO exerciseDTO,
            ExerciseRepository exerciseRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.exerciseDTO = exerciseDTO;
        this.exerciseRepository = exerciseRepository;
    }

    public Training createdTraining(
            TrainingDTO dto
    ) {
        Training training = new Training();

         training.setId(UUID.randomUUID());
         training.setData(dto.data());
         training.setUserId(dto.userId());
         training.setStatus(dto.status());
         training.setTraining_name(dto.training_name());

        return trainingRepository.save(training);
    }

    public Exercise addExercises(
            ExerciseDTO dto,
            UUID trainingId
    ) {
        List<Exercise> exercises = new ArrayList<>();

        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID());
        exercise.setTraining_id(trainingId);
        exercise.setName_exercise(dto.name_exercise());
        exercise.setNotes(dto.notes());

        exercises.add(exercise);
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
        training.setStatus(dto.status());

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

