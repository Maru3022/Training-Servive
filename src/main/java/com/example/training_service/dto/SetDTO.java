package com.example.training_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetDTO {

    private UUID id;
    private UUID exercise_id;
    private Integer weight;
    private Integer reps;
    private Integer order_;
}
