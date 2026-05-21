DROP TABLE IF EXISTS escrow_votes CASCADE;

CREATE TABLE escrow_votes (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_transaction_id UUID        NOT NULL,
    user_id               UUID        NOT NULL,
    decision              VARCHAR(20) NOT NULL CHECK (decision IN ('APPROVED', 'REJECTED')),
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_escrow_votes_transaction FOREIGN KEY (escrow_transaction_id) REFERENCES escrow_transactions (id) ON DELETE CASCADE,
    
    CONSTRAINT uq_escrow_votes_transaction_user UNIQUE (escrow_transaction_id, user_id)
);

CREATE INDEX idx_escrow_votes_transaction_id ON escrow_votes (escrow_transaction_id);