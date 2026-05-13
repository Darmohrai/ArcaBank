package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.NotEnoughMoneyEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;
import com.arcabank.core_finance.utils.RoutingRegistry;

import java.util.UUID;

@NotificationHandler(event = NotEnoughMoneyEvent.class)
public class NotEnoughMoneyStrategy implements NotificationStrategy<NotEnoughMoneyEvent> {

    @Override
    public BaseNotification buildNotification(NotEnoughMoneyEvent event) {
        BaseNotification notification = new BaseNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.NOT_ENOUGH_MONEY);
        notification.setActionType(ActionType.NAVIGATE);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateNotEnoughMoneyTitle());
        notification.setText(TemplateTextCreator.generateNotEnoughMoneyText(event.amount(), event.currency()));

        notification.setEndpoint(RoutingRegistry.AppRoute.ACCOUNT_DETAILS.build(""));

        return notification;
    }
}
