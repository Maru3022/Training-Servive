package com.example.training_service;

import com.example.training_service.model.TrainingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TrainingBulkLoader {

    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final List<Long> batchDurations = new ArrayList<>();

    public TrainingBulkLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void runBulkLoad() {
        runBulkLoad(1000000, 5000);
    }

    public void runBulkLoad(int totalRecords, int batchSize) {
        batchDurations.clear();
        TrainingStatus[] statuses = TrainingStatus.values();

        String sql = "INSERT INTO training (id, data, user_id, status, training_name) VALUES (?, ?, ?, ?, ?)";

        log.info("Starting manual bulk insert of {} records...", totalRecords);
        long totalStartTime = System.currentTimeMillis();

        for (int i = 0; i < totalRecords; i += batchSize) {
            final int currentBatchStart = i;
            long batchStart = System.nanoTime();

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int j) throws java.sql.SQLException {
                    ps.setObject(1, UUID.randomUUID());
                    long randomDay = ThreadLocalRandom.current().nextLong(365);
                    ps.setDate(2, Date.valueOf(LocalDate.now().minusDays(randomDay)));
                    ps.setObject(3, UUID.randomUUID());
                    ps.setString(4, statuses[ThreadLocalRandom.current().nextInt(statuses.length)].name());
                    ps.setString(5, "Workout_" + UUID.randomUUID().toString().substring(0, 8));
                }

                @Override
                public int getBatchSize() {
                    return Math.min(batchSize, totalRecords - currentBatchStart);
                }
            });

            long batchEnd = System.nanoTime();
            batchDurations.add(batchEnd - batchStart);

            if (i > 0 && i % 100000 == 0) {
                log.info("Progress: {}/{} records inserted...", i, totalRecords);
            }
        }

        long totalEndTime = System.currentTimeMillis();
        printStatistics(totalEndTime - totalStartTime, totalRecords);
    }

    private void printStatistics(long totalTimeMs, int totalRecords) {
        Collections.sort(batchDurations);
        int size = batchDurations.size();

        double p50 = batchDurations.get((int) (size * 0.50)) / 1_000_000.0;
        double p95 = batchDurations.get((int) (size * 0.95)) / 1_000_000.0;
        double p99 = batchDurations.get((int) (size * 0.99)) / 1_000_000.0;
        double throughput = totalRecords / (totalTimeMs / 1000.0);

        log.info("\n--- Benchmark Results ---");
        log.info("Total time: {} s", totalTimeMs / 1000.0);
        log.info("Throughput: {} records/sec", String.format("%.2f", throughput));
        log.info("Average Batch Latency (p50): {} ms", p50);
        log.info("Tail Latency (p95): {} ms", p95);
        log.info("Worst Latency (p99): {} ms", p99);
        log.info("--------------------------\n");
    }
}