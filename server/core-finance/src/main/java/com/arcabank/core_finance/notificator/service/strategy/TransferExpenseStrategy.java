package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.TransferExpenseEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;
import com.arcabank.core_finance.utils.RoutingRegistry;

import java.util.UUID;

@NotificationHandler(event = TransferExpenseEvent.class)
public class TransferExpenseStrategy implements NotificationStrategy<TransferExpenseEvent> {

    @Override
    public BaseNotification buildNotification(TransferExpenseEvent event) {
        BaseNotification notification = new BaseNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.TRANSFER_EXPENSE);
        notification.setActionType(ActionType.NAVIGATE);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateTransferExpenseTitle());
        notification.setText(TemplateTextCreator.generateTransferExpenseText(event.amount(), event.currency()));

        notification.setEndpoint(RoutingRegistry.AppRoute.TRANSFERS_HISTORY.build());

        return notification;
    }
}
