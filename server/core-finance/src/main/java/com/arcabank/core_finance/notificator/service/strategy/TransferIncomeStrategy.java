package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.annotation.NotificationHandler;
import com.arcabank.core_finance.notificator.event.TransferIncomeEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;
import com.arcabank.core_finance.notificator.model.template.TemplateTextCreator;
import com.arcabank.core_finance.notificator.model.template.TemplateTitleCreator;
import com.arcabank.core_finance.notificator.model.type.ActionType;
import com.arcabank.core_finance.notificator.model.type.NotificationType;
import com.arcabank.core_finance.utils.RoutingRegistry;

import java.util.UUID;

@NotificationHandler(event = TransferIncomeEvent.class)
public class TransferIncomeStrategy implements NotificationStrategy<TransferIncomeEvent> {

    @Override
    public BaseNotification buildNotification(TransferIncomeEvent event) {
        BaseNotification notification = new BaseNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.TRANSFER_INCOME);
        notification.setActionType(ActionType.NAVIGATE);
        notification.setUserEmail(event.userEmail());
        notification.setTitle(TemplateTitleCreator.generateTransferIncomeTitle());
        notification.setText(TemplateTextCreator.generateTransferIncomeText(event.amount(), event.currency()));

        notification.setEndpoint(RoutingRegistry.AppRoute.TRANSFERS_HISTORY.build());

        return notification;
    }
}
