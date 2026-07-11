package com.zinoviev.conversion_microservice.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinoviev.conversion_microservice.messaging.event.ConversionProcessedEvent;
import com.zinoviev.conversion_microservice.outbox.dao.OutboxRepository;
import com.zinoviev.conversion_microservice.outbox.model.Outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(UUID messageKey, String topicName, String payload) {
        Outbox outbox = new Outbox(messageKey, topicName, payload);
        outboxRepository.save(outbox);
    }

    @Override
    @Transactional
    public void sendToKafka() throws ExecutionException, InterruptedException, JsonProcessingException {
        List<Outbox> outboxList = outboxRepository.findAll();

        for (Outbox outbox : outboxList) {
            ConversionProcessedEvent event =
                    objectMapper.readValue(
                            outbox.getPayload(),
                            ConversionProcessedEvent.class);

            ProducerRecord<String, Object> record =
                    new ProducerRecord<>(
                            outbox.getTopicName(),
                            outbox.getMessageKey().toString(),
                            event);

            record.headers().add("message_key", outbox.getMessageKey().toString().getBytes());

            kafkaTemplate.send(record).get();
            log.info("Сообщение в {} успешно отправлено, message_key: {}", outbox.getTopicName(), outbox.getMessageKey());

            outboxRepository.deleteById(outbox.getId());
        }
    }
}
