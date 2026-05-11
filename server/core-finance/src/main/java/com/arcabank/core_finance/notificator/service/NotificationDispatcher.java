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

    public NotificationDispatcher(List<NotificationStrategy<?>> strategies,
                                  NotificationOutboxRepository outboxRepository,
                                  ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;

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

    //todo read about eventlistener
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void processEvent(NotificationEvent event) {
        NotificationStrategy strategy = strategyRegistry.get(event.getClass());

        if (strategy != null) {
            @SuppressWarnings("unchecked")
            BaseNotification notification = ((NotificationStrategy<NotificationEvent>) strategy).buildNotification(event);

            try {
                String payloadAsJson = objectMapper.writeValueAsString(notification);

                NotificationOutbox outboxMessage = NotificationOutbox.builder()
                    .id(UUID.randomUUID())
                    .topic("system.notifications")
                    .payload(payloadAsJson)
                    .status(NotificationOutbox.OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

                outboxRepository.insert(outboxMessage);

                log.debug("Successfully saved notification event to outbox for user {}", notification.getUserId());

            } catch (JsonProcessingException e) {
                log.error("Failed to serialize NotificationPayload to JSON for event {}", event.getClass().getSimpleName(), e);
            }
        } else {
            log.warn("No NotificationStrategy found for event: {}", event.getClass().getSimpleName());
        }
    }
}
