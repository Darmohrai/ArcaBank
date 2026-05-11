package com.arcabank.core_finance.notificator.service;

import com.arcabank.core_finance.notificator.model.outbox.NotificationOutbox;
import com.arcabank.core_finance.notificator.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaOutboxService {

    private final NotificationOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void sendPendingImmediately() {
        List<NotificationOutbox> pending = outboxRepository.findPendingToRetry(5);
        for (NotificationOutbox message : pending) {
            sendToKafka(message);
        }
    }

    private void sendToKafka(NotificationOutbox message) {
        kafkaTemplate.send(message.getTopic(), message.getId().toString(), message.getPayload())
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    outboxRepository.updateStatusAndRetry(message.getId(), NotificationOutbox.OutboxStatus.SENT, message.getRetryCount());
                } else {
                    outboxRepository.updateStatusAndRetry(message.getId(), NotificationOutbox.OutboxStatus.FAILED, message.getRetryCount() + 1);
                    log.error("Kafka delivery failed for {}", message.getId());
                }
            });
    }
}
