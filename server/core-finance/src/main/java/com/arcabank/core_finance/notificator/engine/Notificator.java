package com.arcabank.core_finance.notificator.engine;

import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.notificator.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class Notificator {

    private final ApplicationEventPublisher eventPublisher; // todo create custom for notification

    public void notifyAccountCreated(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getUserId(),
                account.getCurrency(),
                account.getType(),
                account.getIban(),
                "email@email" //todo fix email
        );

        eventPublisher.publishEvent(event);
    }

    public void notifyAccountBlocked(Account account) {
        AccountBlockedEvent event = new AccountBlockedEvent(
                account.getUserId(),
                account.getIban(),
                "email@email"
        );

        eventPublisher.publishEvent(event);
    }

    public void notifyAccountUnblocked(Account account) {
        AccountUnblockedEvent event = new AccountUnblockedEvent(
                account.getUserId(),
                account.getId(),
                account.getIban(),
                "email@email"
        );

        eventPublisher.publishEvent(event);
    }

    public void notifyCardCreated(UUID userId, UUID cardId, String pan) {
        String maskedPan = "**** " + pan.substring(pan.length() - 4);

        CardCreatedEvent event = new CardCreatedEvent(
            userId,
            cardId,
            maskedPan,
            "email@email" // todo:
        );

        eventPublisher.publishEvent(event);
    }

    public void notifyTransferSuccess(UUID senderId, UUID receiverId, java.math.BigDecimal amount, String currency) {
        TransferExpenseEvent expenseEvent = new TransferExpenseEvent(
            senderId, amount, currency, "email@email"
        );
        eventPublisher.publishEvent(expenseEvent);

        TransferIncomeEvent incomeEvent = new TransferIncomeEvent(
            receiverId, amount, currency, "email@email"
        );
        eventPublisher.publishEvent(incomeEvent);
    }
}
