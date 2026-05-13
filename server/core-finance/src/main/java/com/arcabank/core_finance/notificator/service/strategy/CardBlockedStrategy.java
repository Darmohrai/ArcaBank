package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.CardBlockedEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;
import com.arcabank.core_finance.utils.RoutingRegistry;

import java.util.UUID;

@NotificationHandler(event = CardBlockedEvent.class)
public class CardBlockedStrategy implements NotificationStrategy<CardBlockedEvent> {

    @Override
    public BaseNotification buildNotification(CardBlockedEvent event) {
        BaseNotification notification = new BaseNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.CARD_BLOCKED);
        notification.setActionType(ActionType.EXTERNAL_LINK);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateCardBlockedTitle());
        notification.setText(TemplateTextCreator.generateCardBlockedText(event.maskedCardNumber()));

        notification.setEndpoint(RoutingRegistry.AppRoute.CARD_DETAILS.build(event.cardId()));

        return notification;
    }
}
