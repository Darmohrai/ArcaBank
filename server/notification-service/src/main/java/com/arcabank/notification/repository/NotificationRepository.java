package com.arcabank.notification.repository;

import com.arcabank.notification.model.BaseNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository extends BaseRepository<BaseNotification> {

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    private final String insert = """
        INSERT INTO notifications (id, user_id, notification_type, action_type, title, text, endpoint, is_read)
        VALUES (?, ?, ?, ?, ?, ?, ?, false)
        """;

    public void insert(BaseNotification notification) {
        update(insert,
            notification.getId(),
            notification.getUserId(),
            notification.getNotificationType() != null ? notification.getNotificationType().name() : null,
            notification.getActionType() != null ? notification.getActionType().name() : null,
            notification.getTitle(),
            notification.getText(),
            notification.getEndpoint()
        );
    }
}
