package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;

import java.util.UUID;

public interface InboxService {

    boolean isMessageExistsAndCompleted(UUID messageId, Inbox.InboxStatus status);

    void saveMessage(UUID messageId);

}
