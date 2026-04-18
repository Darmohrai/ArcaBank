CREATE TABLE users
(
    id           VARCHAR(255) PRIMARY KEY,
    email        VARCHAR(100) NOT NULL UNIQUE,
    first_name   VARCHAR(50),
    last_name    VARCHAR(50),
    passport_id  VARCHAR(50),
    phone_number VARCHAR(50),
    status       VARCHAR(20) DEFAULT 'ACTIVE',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
