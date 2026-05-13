package com.arcabank.core_finance.notificator.service.strategy;

import com.arcabank.core_finance.notificator.event.NotificationEvent;
import com.arcabank.core_finance.notificator.model.BaseNotification;

public interface NotificationStrategy<T extends NotificationEvent> {
    BaseNotification buildNotification(T event);
}
