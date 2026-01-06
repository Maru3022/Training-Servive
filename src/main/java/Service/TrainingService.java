package Service;

import DTO.TrainingDTO;
import model.Training;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TrainingService {

    public static Training createdTraining(TrainingDTO dto) {
        Training trainings = new Training();

         trainings.setId(UUID.randomUUID());
         trainings.setData(dto.data());
         trainings.setUserId(dto.userId());
         trainings.setStatus(dto.status());

        return trainings;
    }
}

