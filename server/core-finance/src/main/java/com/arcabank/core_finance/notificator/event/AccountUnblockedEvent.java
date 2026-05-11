package com.arcabank.core_finance.notificator.event;

import java.util.UUID;

public record AccountUnblockedEvent(
    UUID userId,
    UUID accountId,
    String iban,
    String userEmail
) implements NotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
