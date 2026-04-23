CREATE
    OR REPLACE FUNCTION sp_create_new_account(
    p_user_id UUID,
    p_iban VARCHAR,
    p_type VARCHAR,
    p_currency VARCHAR DEFAULT 'UAH'
)
    RETURNS UUID

    LANGUAGE plpgsql
AS
$$

DECLARE
    v_new_account_id UUID;

BEGIN

    INSERT INTO accounts (user_id, iban, type, currency)
    VALUES (p_user_id, p_iban, p_type, p_currency)
    RETURNING id
        INTO v_new_account_id;

    RETURN v_new_account_id;

END;

$$;
