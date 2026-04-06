package com.example.training_service;

import org.springframework.stereotype.Component;

@Component
public class TrainingBulkLoader {

    public void runBulkLoad(int count, int batchSize) {
        // Bulk load logic placeholder
        System.out.println("Running bulk load: count=" + count + ", batchSize=" + batchSize);
    }
}
