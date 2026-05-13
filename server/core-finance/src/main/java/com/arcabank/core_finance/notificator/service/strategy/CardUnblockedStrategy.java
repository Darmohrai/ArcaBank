package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.CardUnblockedEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;
import com.arcabank.core_finance.utils.RoutingRegistry;

import java.util.UUID;

@NotificationHandler(event = CardUnblockedEvent.class)
public class CardUnblockedStrategy implements NotificationStrategy<CardUnblockedEvent> {

    @Override
    public BaseNotification buildNotification(CardUnblockedEvent event) {
        BaseNotification notification = new BaseNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.CARD_UNBLOCKED);
        notification.setActionType(ActionType.EXTERNAL_LINK);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateCardUnblockedTitle());
        notification.setText(TemplateTextCreator.generateCardUnblockedText(event.maskedCardNumber()));

        notification.setEndpoint(RoutingRegistry.AppRoute.CARD_DETAILS.build(event.cardId()));

        return notification;
    }
}
