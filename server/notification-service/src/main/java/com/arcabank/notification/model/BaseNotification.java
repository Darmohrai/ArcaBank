package com.arcabank.notification.model;

import com.arcabank.notification.model.type.ActionType;
import com.arcabank.notification.model.type.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BaseNotification {
    private UUID id;

    private NotificationType notificationType;
    private ActionType actionType;

    private UUID userId;
    private String userEmail;
    private String title;
    private String text;
    private String endpoint;

    private boolean isRead;
}
