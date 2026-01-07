package model;

import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Data
public class Set {

    @Id
    private UUID id;
    private UUID exercise_id;
    private int weight;
    private int reps;
    //order - порядковый номер сета(подхода)
    private int order;

}
