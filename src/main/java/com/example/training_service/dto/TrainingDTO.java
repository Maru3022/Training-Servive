package com.example.training_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {

    private UUID id;
    private LocalDate training_date;
    private UUID user_id;
    private String training_name;
    private String training_status;
    private List<SetDTO> sets;
}
