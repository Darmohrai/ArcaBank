DROP TABLE IF EXISTS chest_members CASCADE;
DROP TABLE IF EXISTS chests CASCADE;

CREATE TABLE chests
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255)   NOT NULL,
    target_amount  NUMERIC(15, 2) NOT NULL CHECK (target_amount > 0),
    description    TEXT,
    currency       VARCHAR(3)       DEFAULT 'UAH',
    balance        NUMERIC(15, 2)   DEFAULT 0.00 CHECK (balance >= 0),
    status         VARCHAR(20)      DEFAULT 'ACTIVE',
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chest_members
(
    chest_id  UUID        NOT NULL,
    user_id   UUID        NOT NULL,
    role      VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'TRUSTEE', 'VIEWER')),
    joined_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_chest FOREIGN KEY (chest_id) REFERENCES chests (id) ON DELETE CASCADE,
    CONSTRAINT uq_chest_members_chest_user UNIQUE (chest_id, user_id)
);

CREATE INDEX idx_chest_members_user_id ON chest_members (user_id);