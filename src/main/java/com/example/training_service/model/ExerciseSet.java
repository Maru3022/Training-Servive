package com.example.training_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "exercise_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private UUID exercise_id;

    @Column
    private Integer weight;

    @Column
    private Integer reps;

    @Column(name = "order_")
    private Integer order_;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    @JsonIgnore
    private Training training;
}
