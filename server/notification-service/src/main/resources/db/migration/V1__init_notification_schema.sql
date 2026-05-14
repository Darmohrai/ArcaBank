CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE notifications
(
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    action_type       VARCHAR(50)  NOT NULL,
    title             VARCHAR(255) NOT NULL,
    text              TEXT         NOT NULL,
    endpoint          VARCHAR(255),
    is_read           BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id) WHERE is_read = FALSE;