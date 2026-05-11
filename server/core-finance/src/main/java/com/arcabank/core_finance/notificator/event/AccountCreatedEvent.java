package com.arcabank.core_finance.notificator.event;

import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.model.util.Currency;

import java.util.UUID;

public record AccountCreatedEvent(
    UUID userId,
    Currency currency,
    AccountType accountType,
    String iban,
    String userEmail
) implements NotificationEvent {

    @Override
    public UUID getUserId() {
        return this.userId;
    }
}
