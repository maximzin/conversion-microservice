package com.zinoviev.conversion_microservice.messaging.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinoviev.conversion_microservice.conversion.service.ConversionService;
import com.zinoviev.conversion_microservice.core.exception.UnknownMessageStatusException;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import com.zinoviev.conversion_microservice.messaging.event.ConversionCreatedEvent;
import com.zinoviev.conversion_microservice.messaging.event.ConversionEventStatus;
import com.zinoviev.conversion_microservice.messaging.event.ConversionProcessedEvent;
import com.zinoviev.conversion_microservice.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
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
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) throws JsonProcessingException {

        UUID uuidMessageKey = UUID.fromString(messageKey);
        log.info("Получено сообщение с messageKey: {}", uuidMessageKey);

        // Проверяем таблицу Inbox
        Optional<Inbox> existingInbox = inboxService.findByMessageKey(uuidMessageKey);
        if (existingInbox.isPresent()) {
            switch (existingInbox.get().getStatus()) {
                case RECEIVED:
                case PROCESSING:
                case COMPLETED: {
                    log.warn("Получен дубликат сообщения {}, статус: {}, не обрабатываем его", uuidMessageKey, existingInbox.get().getStatus());
                    return;
                }
                case FAILED: {
                    log.info("Получен дубликат сообщения {}, статус: {}, будет произведена повторная попытка", uuidMessageKey, existingInbox.get().getStatus());
                    existingInbox.get().setStatus(Inbox.InboxStatus.RECEIVED);
                    uuidMessageKey = existingInbox.get().getMessageKey();
                    break;
                }
                default: {
                    log.error("Получен дубликат сообщения {}, статус: {}, статус неизвествен", uuidMessageKey, existingInbox.get().getStatus());
                    throw new UnknownMessageStatusException("Неизвестный статус сообщения в таблице Inbox");
                }
            }
        } else {
            // Если сообщение новое, то сохраняем
            inboxService.saveMessage(uuidMessageKey);
        }

        // Начинаем обработку сообщения
        inboxService.updateStatus(uuidMessageKey, Inbox.InboxStatus.PROCESSING);

        try {
            // Получаем список ключей сконвертированных файлов
            List<String> convertedFileKeys = conversionService.convertFileToPdf(event.originalFileKey());

            // Посылаем результаты в Kafka
            for (String convertedFileKey : convertedFileKeys) {

                ConversionProcessedEvent conversionProcessedEvent = new ConversionProcessedEvent(
                        UUID.randomUUID(),
                        ConversionEventStatus.COMPLETED,
                        event.originalFileKey(),
                        convertedFileKey,
                        null,
                        LocalDateTime.now());

                String payload = objectMapper.writeValueAsString(conversionProcessedEvent);

                outboxService.save(uuidMessageKey, conversionProcessedEventsTopicName, payload);

                inboxService.updateStatus(uuidMessageKey, Inbox.InboxStatus.COMPLETED);
                inboxService.updateProcessedAt(uuidMessageKey);
            }
        } catch (Exception e) {
            inboxService.updateStatus(uuidMessageKey, Inbox.InboxStatus.FAILED);
            inboxService.updateProcessedAt(uuidMessageKey);
            log.info("Произошла ошибка при работе с сообщением, messageKey: {}, присвоен статус FAILED", uuidMessageKey, e);

            ConversionProcessedEvent conversionProcessedEvent = new ConversionProcessedEvent(
                    UUID.randomUUID(),
                    ConversionEventStatus.FAILED,
                    event.originalFileKey(),
                    null,
                    String.format("%s : %s",e.getClass().getSimpleName(), e.getLocalizedMessage()),
                    LocalDateTime.now());

            String payload = objectMapper.writeValueAsString(conversionProcessedEvent);

            outboxService.save(uuidMessageKey, conversionProcessedEventsTopicName, payload);
        }
    }

}
