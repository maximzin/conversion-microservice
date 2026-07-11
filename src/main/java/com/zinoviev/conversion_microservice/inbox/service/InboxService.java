package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;

import java.util.Optional;
import java.util.UUID;

public interface InboxService {

    Optional<Inbox> findByMessageKey(UUID messageKey);

    void saveMessage(UUID messageKey);

    void updateStatus(UUID messageKey, Inbox.InboxStatus status);

    void updateProcessedAt(UUID messageKey);

    void cleanInboxTableByOldLimitDateTime(int durationHoursToDelete);

}
