package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "exercise_sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSet {

    @Id
    @GeneratedValue
    private UUID id;

    private Integer weight;
}
