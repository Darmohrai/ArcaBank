CREATE TABLE exchange_rates (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                currency VARCHAR(3) NOT NULL UNIQUE,
                                base_currency VARCHAR(3) DEFAULT 'UAH',
                                buy_rate NUMERIC(15, 4) NOT NULL,
                                sell_rate NUMERIC(15, 4) NOT NULL,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE transactions
    ADD COLUMN exchange_rate NUMERIC(15, 4),
ADD COLUMN converted_amount NUMERIC(15, 2),
ADD COLUMN receiver_currency VARCHAR(3);
