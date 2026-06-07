-- 1. Додаємо колонку type у таблицю transactions.
-- DEFAULT 'TRANSFER' гарантує, що старі записи не зламаються, якщо вони вже є в базі.
ALTER TABLE transactions
    ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'TRANSFER';

-- 2. Оновлюємо нашу збережену процедуру (завдяки OR REPLACE вона просто перезапише стару версію)
CREATE OR REPLACE PROCEDURE sp_process_chest_deposit(
    p_account_id UUID,
    p_chest_id UUID,
    p_amount NUMERIC
)
    LANGUAGE plpgsql
AS $$
DECLARE
v_currency VARCHAR(3);
BEGIN

SELECT currency INTO v_currency
FROM accounts
WHERE id = p_account_id;

UPDATE accounts
SET balance = balance - p_amount
WHERE id = p_account_id;

UPDATE chests
SET balance = balance + p_amount
WHERE id = p_chest_id;

INSERT INTO transactions (
    sender_account_id,
    receiver_account_id,
    amount,
    currency,
    status,
    exchange_rate,
    converted_amount,
    receiver_currency,
    type
) VALUES (
             p_account_id,
             p_chest_id,
             p_amount,
             v_currency,
             'SUCCESS',
             1.00,
             p_amount,
             v_currency,
             'CHEST_DEPOSIT'
         );
END;
$$;
