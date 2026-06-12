package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.AccountUnblockedEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;

import java.util.UUID;

@NotificationHandler(event = AccountUnblockedEvent.class)
public class AccountUnblockedStrategy implements NotificationStrategy<AccountUnblockedEvent> {

    @Override
    public BaseNotification buildNotification(AccountUnblockedEvent event) {
        BaseNotification notification = new BaseNotification();

        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.ACCOUNT_UNBLOCKED);
        notification.setActionType(ActionType.EXTERNAL_LINK);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateAccountUnblockedTitle());
        notification.setText(TemplateTextCreator.generateAccountUnblockedText(event.iban()));
        notification.setEndpoint("/");

        return notification;
    }
}
