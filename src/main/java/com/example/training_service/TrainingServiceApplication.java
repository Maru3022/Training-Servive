package com.example.training_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

//ToDo: изучить работу redis от Паши

//ToDo: CI/CD
//ToDo:Frontend

@EnableCaching
@SpringBootApplication
public class TrainingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingServiceApplication.class, args);

    }

}
