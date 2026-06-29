package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.dao.InboxRepository;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class InboxServiceImpl implements InboxService {

    private final InboxRepository inboxRepository;

    public InboxServiceImpl(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @Override
    @Transactional
    public Optional<Inbox> findByMessageId(UUID messageId) {
        return inboxRepository.findByMessageId(messageId);
    }

    @Override
    @Transactional
    public void saveMessage(UUID messageId) {
        Inbox inbox = new Inbox(messageId);
        inboxRepository.save(inbox);
    }

    @Override
    @Transactional
    public void updateStatus(UUID messageId, Inbox.InboxStatus status) {
        inboxRepository.setNewStatusForMessage(messageId, status);
    }

    @Override
    @Transactional
    public void updateProcessedAt(UUID messageId) {
        inboxRepository.setProcessedAtForMessage(messageId, LocalDateTime.now());
    }
}
