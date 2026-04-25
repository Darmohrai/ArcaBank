CREATE TABLE accounts
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    iban       VARCHAR(34) NOT NULL UNIQUE,
    type       VARCHAR(20) NOT NULL,
    currency   VARCHAR(3)       DEFAULT 'UAH',
    balance    NUMERIC(15, 2)   DEFAULT 0.00 CHECK (balance >= 0),
    status     VARCHAR(20)      DEFAULT 'ACTIVE',
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);

CREATE TABLE transactions
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_account_id   UUID           NOT NULL,
    receiver_account_id UUID,
    amount              NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    currency            VARCHAR(3)     NOT NULL,
    status              VARCHAR(20)      DEFAULT 'PENDING',
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sender FOREIGN KEY (sender_account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_account_id) REFERENCES accounts (id) ON DELETE RESTRICT
);
CREATE INDEX idx_transactions_sender ON transactions (sender_account_id);
CREATE INDEX idx_transactions_receiver ON transactions (receiver_account_id);

CREATE TABLE cards
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id       UUID         NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    card_number      VARCHAR(16)  NOT NULL UNIQUE,
    card_holder_name VARCHAR(100) NOT NULL,
    expiration_date  VARCHAR(5)   NOT NULL,
    cvv_hash         VARCHAR(255) NOT NULL,
    status           VARCHAR(20)      DEFAULT 'ACTIVE',
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cards_account_id ON cards (account_id);

CREATE TABLE chests
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id       UUID         NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    type           VARCHAR(20)  NOT NULL,
    currency       VARCHAR(3)       DEFAULT 'UAH',
    balance        NUMERIC(15, 2)   DEFAULT 0.00 CHECK (balance >= 0),
    frozen_balance NUMERIC(15, 2)   DEFAULT 0.00 CHECK (frozen_balance >= 0),
    status         VARCHAR(20)      DEFAULT 'ACTIVE',
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_chests_owner_id ON chests (owner_id);

CREATE TABLE chest_members
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chest_id  UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    role      VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chest FOREIGN KEY (chest_id) REFERENCES chests (id) ON DELETE CASCADE,
    UNIQUE (chest_id, user_id)
);
CREATE INDEX idx_chest_members_user_id ON chest_members (user_id);

CREATE TABLE escrow_transactions
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chest_id          UUID           NOT NULL,
    initiator_id      UUID           NOT NULL,
    target_account_id UUID           NOT NULL,
    amount            NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    required_votes    INT            NOT NULL CHECK (required_votes > 0),
    status            VARCHAR(20)      DEFAULT 'PENDING',
    timeout_at        TIMESTAMP      NOT NULL,
    created_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_escrow_chest FOREIGN KEY (chest_id) REFERENCES chests (id) ON DELETE RESTRICT,
    CONSTRAINT fk_escrow_target FOREIGN KEY (target_account_id) REFERENCES accounts (id) ON DELETE RESTRICT
);
CREATE INDEX idx_escrow_chest_id ON escrow_transactions (chest_id);
CREATE INDEX idx_escrow_status ON escrow_transactions (status);

CREATE TABLE escrow_votes
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escrow_id UUID        NOT NULL,
    voter_id  UUID        NOT NULL,
    decision  VARCHAR(10) NOT NULL,
    voted_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vote_escrow FOREIGN KEY (escrow_id) REFERENCES escrow_transactions (id) ON DELETE CASCADE
);
CREATE INDEX idx_escrow_votes_escrow_id ON escrow_votes (escrow_id);
