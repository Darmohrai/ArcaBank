package com.arcabank.notification.service;

import com.arcabank.notification.model.BaseNotification;
import com.arcabank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessingService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // todo change to netty

    @Transactional
    public void processAndSave(BaseNotification event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
            log.warn("Received notification without ID from Kafka. Generated new UUID: {}", event.getId());
        }

        try {
            notificationRepository.insert(event);

            log.info("Successfully processed and saved notification {} for user {}", event.getId(), event.getUserId());

            String userDestination = "/topic/user." + event.getUserId().toString();
            messagingTemplate.convertAndSend(userDestination, event);
            log.debug("Pushed notification to WebSocket destination: {}", userDestination);

        } catch (DuplicateKeyException e) {
            log.warn("Duplicate notification {} received from Kafka. Skipping insert.", event.getId());
        } catch (Exception e) {
            log.error("Failed to save notification {} to database. User: {}", event.getId(), event.getUserId(), e);
            throw e;
        }
    }
}
