CREATE SEQUENCE IF NOT EXISTS seq_account_internal_number
    START WITH 1000000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE OR REPLACE FUNCTION fn_create_account_with_card(
    p_user_id UUID,
    p_account_type VARCHAR,
    p_currency VARCHAR,
    p_iban VARCHAR,
    p_card_number VARCHAR,
    p_card_holder_name VARCHAR,
    p_expiration_date VARCHAR,
    p_cvv_hash VARCHAR,
    OUT new_account_id UUID,
    OUT new_card_id UUID
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO accounts (user_id, iban, type, currency, balance, status)
    VALUES (p_user_id, p_iban, p_account_type, p_currency, 0.00, 'ACTIVE')
    RETURNING id INTO new_account_id;

    INSERT INTO cards (account_id, card_number, card_holder_name, expiration_date, cvv_hash, status)
    VALUES (new_account_id, p_card_number, p_card_holder_name, p_expiration_date, p_cvv_hash, 'ACTIVE')
    RETURNING id INTO new_card_id;

EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'DUPLICATE_DATA';
END;
$$;