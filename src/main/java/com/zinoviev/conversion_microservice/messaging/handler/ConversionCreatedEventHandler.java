package com.zinoviev.conversion_microservice.messaging.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinoviev.conversion_microservice.common.exception.UnknownMessageStatusException;
import com.zinoviev.conversion_microservice.conversion.service.ConversionService;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import com.zinoviev.conversion_microservice.messaging.event.ConversionCreatedEvent;
import com.zinoviev.conversion_microservice.messaging.event.ConversionProcessedEvent;
import com.zinoviev.conversion_microservice.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        topics = "${topic.conversion.created.events}",
        groupId = "${spring.kafka.consumer.group-id}")
public class ConversionCreatedEventHandler {

    @Value("${topic.conversion.processed.events}")
    private String conversionProcessedEventsTopicName;

    private final InboxService inboxService;
    private final ConversionService conversionService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaHandler
    public void handle(
            @Payload ConversionCreatedEvent event,
            @Header("messageId") String messageId) {

        UUID uuidMessageId = UUID.fromString(messageId);
        log.info("Получено сообщение с messageId: {}", uuidMessageId);

        // Проверяем таблицу Inbox
        Optional<Inbox> existingInbox = inboxService.findByMessageId(uuidMessageId);
        if (existingInbox.isPresent()) {
            switch (existingInbox.get().getStatus()) {
                case RECEIVED:
                case PROCESSING:
                case COMPLETED: {
                    log.warn("Получен дубликат сообщения {}, статус: {}, не обрабатываем его", uuidMessageId, existingInbox.get().getStatus());
                    return;
                }
                case FAILED: {
                    log.info("Получен дубликат сообщения {}, статус: {}, будет произведена повторная попытка", uuidMessageId, existingInbox.get().getStatus());
                    existingInbox.get().setStatus(Inbox.InboxStatus.RECEIVED);
                    uuidMessageId = existingInbox.get().getMessageId();
                    break;
                }
                default: {
                    log.error("Получен дубликат сообщения {}, статус: {}, статус неизвествен", uuidMessageId, existingInbox.get().getStatus());
                    throw new UnknownMessageStatusException("Неизвестный статус сообщения в таблице Inbox");
                }
            }
        } else {
            // Если сообщение новое, то сохраняем
            inboxService.saveMessage(uuidMessageId);
        }

        // Начинаем обработку сообщения
        inboxService.updateStatus(uuidMessageId, Inbox.InboxStatus.PROCESSING);

        try {
            // Получаем список ключей сконвертированных файлов
            List<String> convertedFileKeys = conversionService.convertFileToPdf(event.fileKey());

            // Посылаем результаты в Kafka
            for (String fileKey : convertedFileKeys) {

                ConversionProcessedEvent conversionProcessedEvent = new ConversionProcessedEvent(
                        UUID.randomUUID(),
                        fileKey,
                        LocalDateTime.now());

                String payload = objectMapper.writeValueAsString(conversionProcessedEvent);

                outboxService.save(uuidMessageId, conversionProcessedEventsTopicName, payload);

                inboxService.updateStatus(uuidMessageId, Inbox.InboxStatus.COMPLETED);
                inboxService.updateProcessedAt(uuidMessageId);
            }
        } catch (Exception e) {
            inboxService.updateStatus(uuidMessageId, Inbox.InboxStatus.FAILED);
            inboxService.updateProcessedAt(uuidMessageId);
            log.info("Произошла ошибка при работе с сообщением, messageId: {}, присвоен статус FAILED", uuidMessageId, e);
        }
    }

}
