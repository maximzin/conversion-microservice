package com.zinoviev.conversion_microservice.inbox.dao;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, UUID> {
    Optional<Inbox> findByMessageId(UUID messageId);

    @Modifying
    @Query("""
        UPDATE Inbox i set i.status = :newStatus
        WHERE i.messageId = :messageId
    """)
    void setNewStatusForMessage(@Param("messageId") UUID messageID, @Param("newStatus") Inbox.InboxStatus newStatus);

    @Modifying
    @Query("""
        UPDATE Inbox i set i.processedAt = :processedAt
        WHERE i.messageId = :messageId
    """)
    void setProcessedAtForMessage(@Param("messageId") UUID messageID, @Param("processedAt") LocalDateTime processedAt);
}
