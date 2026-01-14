package com.example.training_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class TrainingBulkLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public TrainingBulkLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        int totalRecords = 1_000_000;
        int batchSize = 5000; // Оптимальный размер для PostgreSQL

        String sql = "INSERT INTO training (id, data, user_id, status, training_name) VALUES (?, ?, ?, ?, ?)";

        System.out.println("Starting bulk insert of 1,000,000 records...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRecords; i += batchSize) {
            final int currentBatchStart = i;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int j) throws java.sql.SQLException {
                    ps.setObject(1, UUID.randomUUID()); // Поле id из Training.java
                    ps.setDate(2, Date.valueOf(LocalDate.now())); // Поле data
                    ps.setObject(3, UUID.randomUUID()); // Поле user_id
                    ps.setString(4, "PLANNED"); // Из вашего Enum TrainingStatus
                    ps.setString(5, "Mass Training " + (currentBatchStart + j)); // Поле training_name
                }

                @Override
                public int getBatchSize() {
                    return Math.min(batchSize, totalRecords - currentBatchStart);
                }
            });

            if (i % 50000 == 0) {
                System.out.println("Inserted " + i + " records...");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Finished! Total time: " + (endTime - startTime) / 1000 + " seconds.");
    }
}