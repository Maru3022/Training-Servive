package com.example.training_service.kafka;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String TRAINING_CREATED = "training.created";
    public static final String TRAINING_STATUS_CHANGED = "training.status-changed";

    public static final String SAGA_TRAINS_COMMAND = "saga-trains-command";
    public static final String SAGA_TRAINS_RESPONSE = "saga-trains-response";
}