package com.arcabank.core_finance.notificator.model.outbox;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationOutbox {
    private UUID id;
    private String topic;
    private String payload;
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum OutboxStatus {
        PENDING, SENT, FAILED
    }
}
