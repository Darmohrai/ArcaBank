package com.arcabank.core_finance.notificator.event;

import java.util.UUID;

public record CardBlockedEvent(
    UUID userId,
    UUID cardId,
    String maskedCardNumber,
    String userEmail
) implements NotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
