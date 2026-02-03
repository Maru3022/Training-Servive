package com.example.training_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TrainingServiceApplication {

    //ToDo: проверить чтобы не было проблемы N + 1
    public static void main(String[] args) {
        SpringApplication.run(TrainingServiceApplication.class, args);

    }

}
