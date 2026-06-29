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
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InboxStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum InboxStatus {
        RECEIVED, PROCESSING, COMPLETED, FAILED
    }

    public Inbox(UUID messageId) {
        this.messageId = messageId;
        this.status = InboxStatus.RECEIVED;
        this.setCreatedAt(LocalDateTime.now());
    }
}
