package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.inbox.dao.InboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InboxServiceImpl implements InboxService {

    private final InboxRepository inboxRepository;

    public InboxServiceImpl(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @Override
    @Transactional
    public boolean isMessageExistsAndCompleted(UUID messageId, Inbox.InboxStatus status) {
        return inboxRepository.findByMessageIdAndStatus(messageId, status).isPresent();
    }

    @Override
    @Transactional
    public void saveMessage(UUID messageId) {
        Inbox inbox = new Inbox(messageId);
        inboxRepository.save(inbox);
    }
}
