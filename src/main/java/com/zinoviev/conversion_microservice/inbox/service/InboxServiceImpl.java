package com.zinoviev.conversion_microservice.inbox.service;

import com.zinoviev.conversion_microservice.inbox.dao.InboxRepository;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InboxServiceImpl implements InboxService {

    private final InboxRepository inboxRepository;

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

    @Override
    @Transactional
    public void cleanInboxTableByOldLimitDateTime(int durationHoursToDelete) {
        LocalDateTime oldLimitDateTime = LocalDateTime.now().minusHours(durationHoursToDelete);
        int deletedCount = inboxRepository.deleteMessagesByOldLimitDateTime(oldLimitDateTime);
        log.info("Удалено: {} записей из таблицы Inbox, которым более {} часов", deletedCount, durationHoursToDelete);
    }
}
