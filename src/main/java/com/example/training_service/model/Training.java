package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "training")
public class Training {

    @Id
    private UUID id;
    private LocalDate data;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private TrainingStatus status;
    private String training_name;

    @OneToMany(mappedBy = "training",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @ToString.Exclude
    private List<Exercise> exercises = new ArrayList<>();

    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
        exercise.setTraining(this);
    }
}
