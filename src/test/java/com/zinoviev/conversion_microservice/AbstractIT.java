package com.zinoviev.conversion_microservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinoviev.conversion_microservice.conversion.service.ConversionService;
import com.zinoviev.conversion_microservice.inbox.dao.InboxRepository;
import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import com.zinoviev.conversion_microservice.outbox.component.OutboxScheduleSending;
import com.zinoviev.conversion_microservice.outbox.dao.OutboxRepository;
import com.zinoviev.conversion_microservice.outbox.service.OutboxService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@DirtiesContext // нужно закрыть и воссоздать контекст для последующих тестов
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // создаёт один объект вашего теста на весь класс, а не по объекту на каждый метод
@Testcontainers
@EmbeddedKafka(partitions = 1, count = 1, controlledShutdown = true) // параметры Kafka
@SpringBootTest(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
})
public abstract class AbstractIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("conversion_microservice")
            .withUsername("myuser")
            .withPassword("12345");

    @Autowired
    protected InboxService inboxService;
    @Autowired
    protected InboxRepository inboxRepository;

    @MockitoBean
    protected ConversionService conversionService;

    @Autowired
    protected OutboxService outboxService;
    @Autowired
    protected OutboxRepository outboxRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected Environment environment;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker; // Тут подчеркивается красным но ОК

    @Autowired
    protected OutboxScheduleSending outboxScheduleSending;


}
