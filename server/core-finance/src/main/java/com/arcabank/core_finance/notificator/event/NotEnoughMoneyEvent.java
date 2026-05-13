package com.arcabank.core_finance.notificator.event;

import java.math.BigDecimal;
import java.util.UUID;

public record NotEnoughMoneyEvent(
    UUID userId,
    BigDecimal amount,
    String currency,
    String userEmail
) implements FailedNotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
