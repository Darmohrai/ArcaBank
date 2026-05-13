package com.arcabank.core_finance.notificator.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferExpenseEvent(
    UUID userId,
    BigDecimal amount,
    String currency,
    String userEmail
) implements NotificationEvent {
    @Override
    public UUID getUserId() {
        return userId;
    }
}
