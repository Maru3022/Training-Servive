package model;

import jakarta.persistence.Id;

import java.util.UUID;

public class Set {

    @Id
    private UUID id;
    private UUID exercise_id;
    private int weight;
    private int reps;
    private int order;

}
