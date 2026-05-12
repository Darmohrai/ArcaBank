CREATE TABLE notification_outbox
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic       VARCHAR(100) NOT NULL,
    payload     JSONB        NOT NULL,
    status      VARCHAR(20)      DEFAULT 'PENDING',
    retry_count INT              DEFAULT 0,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_outbox_status_retry ON notification_outbox (status, retry_count);
