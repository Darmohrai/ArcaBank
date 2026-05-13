package com.arcabank.core_finance.notificator.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentFailedEvent(
    UUID userId,
    BigDecimal amount,
    String currency,
    String reason,
    String userEmail
) implements FailedNotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
