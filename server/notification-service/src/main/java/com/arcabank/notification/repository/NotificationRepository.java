package com.arcabank.notification.repository;

import com.arcabank.notification.model.BaseNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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

    public void markAsRead(java.util.UUID id) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        update(sql, id);
    }

    public void markAllAsReadForUser(java.util.UUID userId) {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ?";
        update(sql, userId);
    }

    private final RowMapper<BaseNotification> rowMapper = (rs, rowNum) -> {
        BaseNotification n = new BaseNotification();
        n.setId(java.util.UUID.fromString(rs.getString("id")));
        n.setUserId(java.util.UUID.fromString(rs.getString("user_id")));
        n.setNotificationType(com.arcabank.notification.model.type.NotificationType.valueOf(rs.getString("notification_type")));
        n.setActionType(com.arcabank.notification.model.type.ActionType.valueOf(rs.getString("action_type")));
        n.setTitle(rs.getString("title"));
        n.setText(rs.getString("text"));
        n.setEndpoint(rs.getString("endpoint"));
        n.setRead(rs.getBoolean("is_read"));
        return n;
    };

    public List<BaseNotification> findByUserId(UUID userId, int limit, int offset) {
        String sql = """
            SELECT * FROM notifications
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        return queryList(sql, rowMapper, userId, limit, offset);
    }
}
