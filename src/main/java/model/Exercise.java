package model;

import jakarta.persistence.Id;
import org.w3c.dom.Text;

import java.util.UUID;

public class Exercise {

    @Id
    private UUID id;
    private UUID training_id;
    private String name;
    private Text notes;
}
