package com.zinoviev.conversion_microservice.messaging.handler;

import com.zinoviev.conversion_microservice.AbstractIT;
import com.zinoviev.conversion_microservice.inbox.model.Inbox;
import com.zinoviev.conversion_microservice.messaging.event.ConversionCreatedEvent;
import com.zinoviev.conversion_microservice.messaging.event.ConversionProcessedEvent;
import com.zinoviev.conversion_microservice.outbox.model.Outbox;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


class ConversionCreatedEventHandlerIT extends AbstractIT {

    // Слушающий контейнер для исходящих сообщений
    private KafkaMessageListenerContainer<String, ConversionProcessedEvent> containerOut;
    private BlockingQueue<ConsumerRecord<String, ConversionProcessedEvent>> recordsOut;

    @BeforeAll
    void setUp() {
        // Горит красным из-за deprecated, но используя JacksonDeserializer возникает проблема конфликта библиотек "Caused by: java.lang.ClassNotFoundException: tools.jackson.databind.JavaType"
        JsonDeserializer<ConversionProcessedEvent> deserializer =
                new JsonDeserializer<>(ConversionProcessedEvent.class, false);

        deserializer.addTrustedPackages(environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"));

        DefaultKafkaConsumerFactory<String, ConversionProcessedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        getConsumerProperties(),
                        new StringDeserializer(),
                        deserializer
                );

        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("topic.conversion.processed.events"));

        containerOut = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        recordsOut = new LinkedBlockingDeque<>();
        containerOut.setupMessageListener((MessageListener<String, ConversionProcessedEvent>) recordsOut::add);

        containerOut.start();

        ContainerTestUtils.waitForAssignment(containerOut, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id")
        );
    }

    @AfterAll
    void tearDown() {
        containerOut.stop();
        inboxRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @DisplayName("Получить сообщение из топика, обработать и послать в другой топик")
    @SneakyThrows
    @Test
    void handle_shouldGetMessageFromTopicAndProcessAndSendToAnotherTopic() {
        // given
        String readyFileKey = "/processed/file.pdf";

        UUID messageId = UUID.randomUUID();

        ConversionCreatedEvent conversionCreatedEvent = new ConversionCreatedEvent(
                UUID.randomUUID(),
                "randomkey",
                LocalDateTime.now());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                environment.getProperty("topic.conversion.created.events"),
                messageId.toString(),
                conversionCreatedEvent);

        record.headers().add("messageId", messageId.toString().getBytes());

        // when
        when(conversionService.convertFileToPdf(anyString()))
                .thenReturn(new ArrayList<>(List.of(readyFileKey)));

        kafkaTemplate.send(record).get();

        Thread.sleep(2000);

        // then
        Inbox inbox = inboxRepository.findByMessageId(messageId).orElseThrow();
        assertThat(inbox.getStatus()).isEqualTo(Inbox.InboxStatus.COMPLETED);

        List<Outbox> outboxes = outboxRepository.findAll();
        assertThat(outboxes).hasSize(1);

        Thread.sleep(2000);

        // Отправляем в следующий топик сообщения из Outbox таблицы
        outboxScheduleSending.sendMessagesBySchedule();

        Thread.sleep(3000);

        ConsumerRecord<String, ConversionProcessedEvent> incomeMessage = recordsOut.poll(3000, TimeUnit.MILLISECONDS);

        assertNotNull(incomeMessage);
        assertNotNull(incomeMessage.headers());
        Header header = Arrays.stream(incomeMessage.headers().toArray()).findFirst().orElseThrow();
        assertEquals(header.key(), "messageId");

        ConversionProcessedEvent conversionProcessedEvent = incomeMessage.value();
        assertEquals(conversionProcessedEvent.fileKey(), readyFileKey);
    }

    @DisplayName("Получить дубликат сообщения из топика и проигнорировать")
    @SneakyThrows
    @Test
    void handle_shouldGetDuplicateFromTopicAndIgnore() {
        // given
        UUID messageId = UUID.randomUUID();
        Inbox inboxBefore = new Inbox(messageId);
        inboxRepository.save(inboxBefore);

        String readyFileKey = "/processed/file.pdf";

        ConversionCreatedEvent conversionCreatedEvent = new ConversionCreatedEvent(
                UUID.randomUUID(),
                "randomkey",
                LocalDateTime.now());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                environment.getProperty("topic.conversion.created.events"),
                messageId.toString(),
                conversionCreatedEvent);

        record.headers().add("messageId", messageId.toString().getBytes());

        // when
        when(conversionService.convertFileToPdf(anyString()))
                .thenReturn(new ArrayList<>(List.of(readyFileKey)));

        Thread.sleep(2000);

        // then
        List<Inbox> inboxes = inboxRepository.findAll();
        assertThat(inboxes).hasSize(1);

        Inbox inboxAfter = inboxRepository.findByMessageId(messageId).orElseThrow();
        assertThat(inboxAfter.getStatus()).isEqualTo(Inbox.InboxStatus.RECEIVED);
    }
}