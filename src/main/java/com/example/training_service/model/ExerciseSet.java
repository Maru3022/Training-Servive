package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "exercise_sets")
public class ExerciseSet {

    @Id
    private UUID id;

    private int weight;
    private int reps;

    @Column(name = "set_order")
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise; // Это поле "главное", оно само создаст колонку exercise_id
}