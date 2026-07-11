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
    Optional<Inbox> findByMessageKey(UUID messageKey);

    @Modifying
    @Query("""
        UPDATE Inbox i set i.status = :newStatus
        WHERE i.messageKey = :messageKey
    """)
    void setNewStatusForMessage(@Param("messageKey") UUID messageKey, @Param("newStatus") Inbox.InboxStatus newStatus);

    @Modifying
    @Query("""
        UPDATE Inbox i set i.processedAt = :processedAt
        WHERE i.messageKey = :messageKey
    """)
    void setProcessedAtForMessage(@Param("messageKey") UUID messageKey, @Param("processedAt") LocalDateTime processedAt);

    @Modifying
    @Query("""
        DELETE FROM Inbox o
        WHERE o.createdAt <= :oldLimitDateTime
    """)
    int deleteMessagesByOldLimitDateTime(@Param("oldLimitDateTime") LocalDateTime oldLimitDateTime);
}
