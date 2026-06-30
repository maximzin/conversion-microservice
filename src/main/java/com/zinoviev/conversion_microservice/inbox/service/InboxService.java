package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;

import java.util.Optional;
import java.util.UUID;

public interface InboxService {

    Optional<Inbox> findByMessageId(UUID messageId);

    void saveMessage(UUID messageId);

    void updateStatus(UUID messageId, Inbox.InboxStatus status);

    void updateProcessedAt(UUID messageId);

    void cleanInboxTableByOldLimitDateTime(int durationHoursToDelete);

}
