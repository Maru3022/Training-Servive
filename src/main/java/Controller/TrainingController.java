package Controller;

import DTO.TrainingDTO;
import Service.TrainingService;
import model.Training;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrainingController {

    //ToDo:СRUD

    @PostMapping("/trainings")
    public ResponseEntity<Training> postTrainings(@Valid @RequestBody TrainingDTO dto) {

        Training created = TrainingService.createdTraining(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

}
