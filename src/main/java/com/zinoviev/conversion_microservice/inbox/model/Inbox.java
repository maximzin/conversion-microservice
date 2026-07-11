package com.zinoviev.conversion_microservice.inbox.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbox")
@Getter
@Setter
@NoArgsConstructor
public class Inbox {

    @Id
    @Column(name = "message_key", updatable = false, nullable = false)
    private UUID messageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InboxStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum InboxStatus {
        RECEIVED, PROCESSING, COMPLETED, FAILED
    }

    public Inbox(UUID messageKey) {
        this.messageKey = messageKey;
        this.status = InboxStatus.RECEIVED;
        this.setCreatedAt(LocalDateTime.now());
    }
}
