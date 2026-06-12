package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.AccountBlockedEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;

import java.util.UUID;

@NotificationHandler(event = AccountBlockedEvent.class)
public class AccountBlockedStrategy implements NotificationStrategy<AccountBlockedEvent> {

    @Override
    public BaseNotification buildNotification(AccountBlockedEvent event) {
        BaseNotification notification = new BaseNotification();

        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.ACCOUNT_BLOCKED);
        notification.setActionType(ActionType.EXTERNAL_LINK);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateAccountBlockedTitle());
        notification.setText(TemplateTextCreator.generateAccountBlockedText(event.iban()));
        notification.setEndpoint("/");

        return notification;
    }
}
