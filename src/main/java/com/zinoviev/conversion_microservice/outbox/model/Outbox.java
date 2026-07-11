package com.zinoviev.conversion_microservice.outbox.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "message_key", nullable = false)
    private UUID messageKey;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "payload", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Outbox(UUID messageKey, String topicName, String payload) {
        this.messageKey = messageKey;
        this.topicName = topicName;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

}
