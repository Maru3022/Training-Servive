package Controller;

import DTO.TrainingDTO;
import Service.TrainingService;
import model.Training;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class TrainingController {

    //ToDo:СRUD

    @PostMapping("/trainings")
    public ResponseEntity<Training> postTrainings(@RequestBody TrainingDTO dto) {

        Training created = TrainingService.createdTraining(dto);

    }

}
