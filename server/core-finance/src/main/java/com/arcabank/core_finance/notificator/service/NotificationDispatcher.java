package com.arcabank.core_finance.notificator.service;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.NotificationEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.outbox.NotificationOutbox;
import com.arcabank.core_finance.notificator.repository.NotificationOutboxRepository;
import com.arcabank.core_finance.notificator.service.strategy.NotificationStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class NotificationDispatcher {

    private final Map<Class<? extends NotificationEvent>, NotificationStrategy<?>> strategyRegistry = new HashMap<>();

    private final NotificationOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaOutboxService kafkaOutboxService;

    public NotificationDispatcher(List<NotificationStrategy<?>> strategies,
                                  NotificationOutboxRepository outboxRepository,
                                  ObjectMapper objectMapper,
                                  KafkaOutboxService kafkaOutboxService) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.kafkaOutboxService = kafkaOutboxService;

        for (NotificationStrategy<?> strategy : strategies) {
            NotificationHandler annotation = AnnotationUtils.findAnnotation(strategy.getClass(), NotificationHandler.class);

            if (annotation != null) {
                strategyRegistry.put(annotation.event(), strategy);
            } else {
                throw new IllegalStateException("Strategy " + strategy.getClass().getSimpleName() +
                        " must be annotated with @NotificationHandler");
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public NotificationOutbox saveToOutbox(NotificationEvent event) {
        NotificationStrategy<?> strategy = strategyRegistry.get(event.getClass());
        if (strategy == null) return null;

        @SuppressWarnings("unchecked")
        BaseNotification notification = ((NotificationStrategy<NotificationEvent>) strategy).buildNotification(event);

        try {
            NotificationOutbox outboxMessage = NotificationOutbox.builder()
                    .id(UUID.randomUUID())
                    .topic("system.notifications")
                    .payload(objectMapper.writeValueAsString(notification))
                    .status(NotificationOutbox.OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxRepository.insert(outboxMessage);
            return outboxMessage;
        } catch (JsonProcessingException e) {
            log.error("Serialization failed", e);
            return null;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void triggerImmediateSend(NotificationEvent event) {
        kafkaOutboxService.sendPendingImmediately();
    }
}
