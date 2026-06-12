package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.AccountCreatedEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;

import java.util.UUID;

@NotificationHandler(event = AccountCreatedEvent.class)
public class AccountCreatedStrategy implements NotificationStrategy<AccountCreatedEvent> {

    @Override
    public BaseNotification buildNotification(AccountCreatedEvent event) {
        BaseNotification notification = new BaseNotification();

        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.ACCOUNT_CREATED);
        notification.setActionType(ActionType.EXTERNAL_LINK);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateAccountCreatedTitle());
        notification.setText(TemplateTextCreator.generateAccountCreatedText(event.currency()));
        notification.setEndpoint("/");

        return notification;
    }
}
