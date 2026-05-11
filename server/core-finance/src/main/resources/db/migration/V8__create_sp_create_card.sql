CREATE
OR REPLACE FUNCTION fn_create_card(
    p_account_id UUID,
    p_card_number VARCHAR(16),
    p_card_holder_name VARCHAR(100),
    p_expiration_date VARCHAR(5),
    p_cvv_hash VARCHAR(255),
    p_pin_hash VARCHAR(255)
) RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
v_account_status VARCHAR(20);
    v_new_card_id
UUID;
BEGIN
SELECT status
INTO v_account_status
FROM accounts
WHERE id = p_account_id FOR SHARE;

IF
NOT FOUND THEN
        RAISE EXCEPTION 'Account does not exist' USING ERRCODE = 'P0001';
END IF;

    IF
v_account_status != 'ACTIVE' THEN
        RAISE EXCEPTION 'Cannot issue card. Account is not active' USING ERRCODE = 'P0002';
END IF;

INSERT INTO cards (account_id,
                   card_number,
                   card_holder_name,
                   expiration_date,
                   cvv_hash,
                   pin_hash, -- Не забуваємо про PIN!
                   status)
VALUES (p_account_id,
        p_card_number,
        p_card_holder_name,
        p_expiration_date,
        p_cvv_hash,
        p_pin_hash,
        'ACTIVE') RETURNING id
INTO v_new_card_id;

RETURN v_new_card_id;

EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'Card number already exists' USING ERRCODE = 'P0003';
END;
$$;
