package com.arcabank.core_finance.notificator.engine;

import com.arcabank.core_finance.model.Account;
import com.arcabank.core_finance.notificator.event.AccountBlockedEvent;
import com.arcabank.core_finance.notificator.event.AccountCreatedEvent;
import com.arcabank.core_finance.notificator.event.AccountUnblockedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
}
