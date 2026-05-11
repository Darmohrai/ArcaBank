package com.arcabank.core_finance.notificator.event;

import java.util.UUID;

public record CardCreatedEvent(
    UUID userId
    // todo other fields
) implements NotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
