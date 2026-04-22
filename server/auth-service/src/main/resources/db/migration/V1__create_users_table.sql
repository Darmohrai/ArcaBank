CREATE TABLE users
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(100) NOT NULL UNIQUE,
    first_name   VARCHAR(50)  NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    passport_id  VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(50)  NOT NULL,
    status       VARCHAR(20)      DEFAULT 'ACTIVE',
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
