package com.arcabank.core_finance.notificator.repository;

import com.arcabank.core_finance.notificator.model.outbox.NotificationOutbox;
import com.arcabank.core_finance.repository.BaseRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class NotificationOutboxRepository extends BaseRepository<NotificationOutbox> {

    public NotificationOutboxRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    //todo fix mapper
    //todo create sp
    private final RowMapper<NotificationOutbox> rowMapper = (rs, rowNum) ->
        NotificationOutbox.builder()
            .id(rs.getObject("id", UUID.class))
            .topic(rs.getString("topic"))
            .payload(rs.getString("payload"))
            .status(NotificationOutbox.OutboxStatus.valueOf(rs.getString("status")))
            .retryCount(rs.getInt("retry_count"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public void insert(NotificationOutbox outbox) {
        String sql = """
            INSERT INTO notification_outbox (id, topic, payload, status, retry_count)
            VALUES (?, ?, ?::jsonb, ?, ?)
            """;

        update(sql,
            outbox.getId() != null ? outbox.getId() : UUID.randomUUID(),
            outbox.getTopic(),
            outbox.getPayload(),
            outbox.getStatus().name(),
            outbox.getRetryCount()
        );
    }

    public List<NotificationOutbox> findPendingToRetry(int maxRetries) {
        String sql = """
            SELECT * FROM notification_outbox
            WHERE status IN ('PENDING', 'FAILED')
              AND retry_count < ?
            ORDER BY created_at ASC
            """;
        return queryList(sql, rowMapper, maxRetries);
    }

    public void updateStatusAndRetry(UUID id, NotificationOutbox.OutboxStatus status, int retryCount) {
        String sql = """
            UPDATE notification_outbox
            SET status = ?, retry_count = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        update(sql, status.name(), retryCount, id);
    }
}
