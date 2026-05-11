DROP FUNCTION IF EXISTS sp_create_account_with_card(UUID, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);

CREATE OR REPLACE FUNCTION sp_create_account_with_card(
    p_user_id UUID,
    p_account_type VARCHAR,
    p_currency VARCHAR,
    p_iban VARCHAR,
    p_card_number VARCHAR,
    p_card_holder_name VARCHAR,
    p_expiration_date VARCHAR,
    p_cvv_hash VARCHAR,
    p_pin_hash VARCHAR,
    OUT new_account_id UUID,
    OUT new_card_id UUID
)
LANGUAGE plpgsql
AS $$
BEGIN

INSERT INTO accounts (user_id, iban, type, currency, balance, status)
VALUES (p_user_id, p_iban, p_account_type, p_currency, 0.00, 'ACTIVE')
    RETURNING id INTO new_account_id;

INSERT INTO cards (account_id, card_number, card_holder_name, expiration_date, cvv_hash, pin_hash, status)
VALUES (new_account_id, p_card_number, p_card_holder_name, p_expiration_date, p_cvv_hash, p_pin_hash, 'ACTIVE')
    RETURNING id INTO new_card_id;

EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'DUPLICATE_DATA';
END;
$$;
