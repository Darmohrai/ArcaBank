package com.arcabank.notification.service;

import com.arcabank.notification.model.BaseNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaListener {

    private final ObjectMapper objectMapper;
    private final NotificationProcessingService processingService;

    @KafkaListener(topics = "system.notifications", groupId = "notification-group")
    public void consumeNotificationEvent(@Payload String messageJson) {
        log.info("Received new message from Kafka: {}", messageJson);

        try {
            BaseNotification event = objectMapper.readValue(messageJson, BaseNotification.class);

            processingService.processAndSave(event);

        } catch (Exception e) {
            log.error("Failed to process Kafka message. JSON: {}", messageJson, e);
        }
    }
}
