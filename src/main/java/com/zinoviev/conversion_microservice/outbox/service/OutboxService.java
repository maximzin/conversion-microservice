package com.zinoviev.conversion_microservice.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface OutboxService {

    void save(UUID messageKey, String topicName, String payload);

    void sendToKafka() throws ExecutionException, InterruptedException, JsonProcessingException;

}
