CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL, 
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id UUID,                
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id) WHERE is_read = FALSE;
CREATE INDEX idx_notifications_user_date ON notifications(user_id, created_at DESC);