package com.arcabank.core_finance.notificator.event;

import java.util.UUID;

public record AccountBlockedEvent(
    UUID userId,
    String iban,
    String userEmail
) implements NotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
