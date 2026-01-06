package Service;

import DTO.TrainingDTO;
import model.Training;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrainingService {

    public static Training createdTraining(TrainingDTO dto) {
        Training trainings = new Training();

        trainings.setId(UUID.randomUUID());
        trainings.setData(dto.getDate);
        trainings.setUserId(dto.getUserId);
        trainings.setStatus(dto.getStatus);

        return trainings;
    }
}

