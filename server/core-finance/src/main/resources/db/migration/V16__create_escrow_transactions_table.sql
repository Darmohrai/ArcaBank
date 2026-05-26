DROP TABLE IF EXISTS escrow_transactions CASCADE;

CREATE TABLE escrow_transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chest_id            UUID           NOT NULL,
    initiator_id        UUID           NOT NULL,
    amount              NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    destination_account VARCHAR(255)   NOT NULL,
    purpose             TEXT           NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_escrow_transactions_chest FOREIGN KEY (chest_id) REFERENCES chests (id) ON DELETE RESTRICT
);

CREATE INDEX idx_escrow_transactions_chest_id ON escrow_transactions (chest_id);

CREATE UNIQUE INDEX idx_one_pending_escrow ON escrow_transactions (chest_id) WHERE status = 'PENDING';