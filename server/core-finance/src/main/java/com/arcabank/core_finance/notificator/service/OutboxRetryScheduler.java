package com.arcabank.core_finance.notificator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler {
    private final KafkaOutboxService kafkaOutboxService;

    @Scheduled(fixedDelay = 60000)
    public void retry() {
        kafkaOutboxService.sendPendingImmediately();
    }
}
