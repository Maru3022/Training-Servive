package com.example.training_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "trainings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue
    private UUID id;

    private String training_name;
}
