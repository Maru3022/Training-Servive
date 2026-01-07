package model;

import jakarta.persistence.Id;
import lombok.Data;
import org.w3c.dom.Text;

import java.util.UUID;

@Data
public class Exercise {

    @Id
    private UUID id;
    private UUID training_id;
    private String name_exercise;
    private String notes;
}
