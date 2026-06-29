package com.zinoviev.conversion_microservice.messaging.handler;

import com.zinoviev.conversion_microservice.common.exception.UnknownMessageStatusException;
import com.zinoviev.conversion_microservice.conversion.service.ConversionService;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import com.zinoviev.conversion_microservice.messaging.event.ConversionCreatedEvent;
import com.zinoviev.conversion_microservice.messaging.event.ConversionProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
@KafkaListener(
        topics = "${topic.conversion.created.events}",
        groupId = "${spring.kafka.consumer.group-id}")
public class ConversionCreatedEventHandler {

    @Value("${topic.conversion.processed.events}")
    private String conversionProcessedEventsTopicName;

    private final InboxService inboxService;
    private final ConversionService conversionService;
    private final KafkaTemplate<String, ConversionProcessedEvent> kafkaTemplate;

    public ConversionCreatedEventHandler(InboxService inboxService, ConversionService conversionService, KafkaTemplate<String, ConversionProcessedEvent> kafkaTemplate) {
        this.inboxService = inboxService;
        this.conversionService = conversionService;
        this.kafkaTemplate = kafkaTemplate;
    }

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
            List<String> convertedFileKeys = conversionService.convertFileToPdf(event.fileKey());

            // Посылаем результаты в Kafka
            for (String fileKey : convertedFileKeys) {
                ConversionProcessedEvent conversionProcessedEvent = new ConversionProcessedEvent(
                        UUID.randomUUID(),
                        fileKey,
                        LocalDateTime.now()
                );
                ProducerRecord<String, ConversionProcessedEvent> record = new ProducerRecord<>(
                        conversionProcessedEventsTopicName,
                        messageId,
                        conversionProcessedEvent
                );
                record.headers().add("messageId", uuidMessageId.toString().getBytes());

                kafkaTemplate.send(record).get();

                log.info("Сообщение в {} успешно отправлено, messageId: {}", conversionProcessedEventsTopicName, uuidMessageId);
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
