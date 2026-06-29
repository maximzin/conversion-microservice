package com.zinoviev.conversion_microservice.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ConversionCreatedEvent(
        UUID eventId,
        String fileKey,
        Instant createdAt
) {}
