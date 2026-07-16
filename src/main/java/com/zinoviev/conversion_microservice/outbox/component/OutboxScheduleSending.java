package com.zinoviev.conversion_microservice.outbox.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zinoviev.conversion_microservice.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduleSending {

    private final OutboxService outboxService;

    @Scheduled(cron = "${outbox.sending-period-cron}")
    public void sendMessagesBySchedule() throws ExecutionException, InterruptedException, JsonProcessingException {
        log.info("Планировщик Outbox начинает отправку в Kafka");
        outboxService.sendToKafka();
    }
}
