package com.zinoviev.conversion_microservice.messaging.handler;

import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import com.zinoviev.conversion_microservice.messaging.event.ConversionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class ConversionCreatedEventHandler {

    private final InboxService inboxService;

    public ConversionCreatedEventHandler(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @KafkaListener(
            topics = "${topic.conversion.created.events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handle(
            @Payload ConversionCreatedEvent event,
            @Header("messageId") String messageId) {
        UUID uuidMessageId = UUID.fromString(messageId);
        log.info("Получено сообщение с messageId: {}", uuidMessageId);
        // Проверяем таблицу Inbox
        if (inboxService.isMessageExistsAndCompleted(uuidMessageId, Inbox.InboxStatus.COMPLETED)) {
            log.info("Получен дубликат сообщения: {}", uuidMessageId);
            return;
        };

        // Если сообщение новое, то сохраняем
        inboxService.saveMessage(uuidMessageId);

    }

}
