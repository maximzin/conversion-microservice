package com.zinoviev.conversion_microservice.outbox.service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface OutboxService {

    void save(UUID messageId, String topicName, String payload);

    void sendToKafka() throws ExecutionException, InterruptedException;

}
