package com.zinoviev.conversion_microservice.outbox.service;

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

    @Override
    @Transactional
    public void save(UUID messageId, String topicName, String payload) {
        Outbox outbox = new Outbox(messageId, topicName, payload);
        outboxRepository.save(outbox);
    }

    @Override
    @Transactional
    public void sendToKafka() throws ExecutionException, InterruptedException {
        List<Outbox> outboxList = outboxRepository.findAll();

        for (Outbox outbox : outboxList) {
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    outbox.getTopicName(),
                    outbox.getMessageId().toString(),
                    outbox.getPayload());

            record.headers().add("messageId", outbox.getMessageId().toString().getBytes());

            kafkaTemplate.send(record).get();
            log.info("Сообщение в {} успешно отправлено, messageId: {}", outbox.getTopicName(), outbox.getMessageId());

            outboxRepository.deleteById(outbox.getId());
        }
    }
}
