DROP INDEX IF EXISTS idx_transactions_sender;
DROP INDEX IF EXISTS idx_transactions_receiver;

CREATE INDEX idx_transactions_sender_date ON transactions(sender_account_id, created_at DESC);
CREATE INDEX idx_transactions_receiver_date ON transactions(receiver_account_id, created_at DESC);