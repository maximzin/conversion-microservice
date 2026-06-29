package com.zinoviev.conversion_microservice.inbox.dao;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, UUID> {
    Optional<Inbox> findByMessageIdAndStatus(UUID messageId, Inbox.InboxStatus status);
}
