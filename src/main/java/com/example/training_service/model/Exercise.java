package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    private UUID id;

    @Column(name = "name_exercise")
    private String name_exercise;
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id",
            nullable = false)
    @ToString.Exclude
    private Training training;

    @OneToMany(mappedBy = "exercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ExerciseSet> sets = new ArrayList<>();
}
