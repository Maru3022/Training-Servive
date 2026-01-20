package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "exercise_sets", indexes = {
        @Index(name = "idx_set_exercise_id", columnList = "exercise_id")
})
public class ExerciseSet {

    @Id
    private UUID id;

    private int reps;
    private int weight;

    @Column(name = "set_order")
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id",
            nullable = false)
    private Exercise exercise;
}