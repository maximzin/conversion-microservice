package com.zinoviev.conversion_microservice.inbox.component;

import com.zinoviev.conversion_microservice.inbox.service.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxScheduleDeletion {

    @Value("${inbox.duration-hours-to-delete}")
    Integer inboxDurationHoursToDelete;

    private final InboxService inboxService;

    @Scheduled(cron = "${inbox.deletion-period-cron}")
    public void cleanInboxTableBySchedule() {
        log.info("Планировщик Inbox начинает очистку таблицы");
        inboxService.cleanInboxTableByOldLimitDateTime(inboxDurationHoursToDelete);
    }
}
