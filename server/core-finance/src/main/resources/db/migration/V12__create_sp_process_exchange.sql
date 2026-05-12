CREATE
OR REPLACE FUNCTION sp_process_exchange(
    p_sender_id UUID,
    p_receiver_id UUID,
    p_amount_from NUMERIC(15, 2),
    p_amount_to NUMERIC(15, 2),
    p_exchange_rate NUMERIC(15, 4)
) RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
v_sender_record RECORD;
    v_receiver_record
RECORD;
    v_transaction_id
UUID;
BEGIN
    IF
p_amount_from <= 0 OR p_amount_to <= 0 THEN
        RAISE EXCEPTION 'Amounts must be greater than zero' USING ERRCODE = 'P0001';
END IF;

    IF
p_sender_id < p_receiver_id THEN
SELECT *
INTO v_sender_record
FROM accounts
WHERE id = p_sender_id FOR UPDATE;
SELECT *
INTO v_receiver_record
FROM accounts
WHERE id = p_receiver_id FOR UPDATE;
ELSE
SELECT *
INTO v_receiver_record
FROM accounts
WHERE id = p_receiver_id FOR UPDATE;
SELECT *
INTO v_sender_record
FROM accounts
WHERE id = p_sender_id FOR UPDATE;
END IF;

    IF
v_sender_record.balance < p_amount_from THEN
        RAISE EXCEPTION 'Insufficient funds' USING ERRCODE = 'P0005';
END IF;

UPDATE accounts
SET balance = balance - p_amount_from
WHERE id = p_sender_id;
UPDATE accounts
SET balance = balance + p_amount_to
WHERE id = p_receiver_id;

INSERT INTO transactions (sender_account_id, receiver_account_id, amount, currency,
                          status, exchange_rate, converted_amount, receiver_currency)
VALUES (p_sender_id, p_receiver_id, p_amount_from, v_sender_record.currency,
        'SUCCESS', p_exchange_rate, p_amount_to, v_receiver_record.currency) RETURNING id
INTO v_transaction_id;

RETURN v_transaction_id;
END;
$$;
