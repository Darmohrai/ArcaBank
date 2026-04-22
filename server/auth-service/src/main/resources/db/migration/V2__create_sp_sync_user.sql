CREATE
    OR REPLACE PROCEDURE sp_sync_user(
    p_id UUID,
    p_email VARCHAR,
    p_first_name VARCHAR,
    p_last_name VARCHAR,
    p_passport_id VARCHAR,
    p_phone_number VARCHAR
)
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO users (id, email, first_name, last_name, passport_id, phone_number)
    VALUES (p_id, p_email, p_first_name, p_last_name, p_passport_id, p_phone_number)
    ON CONFLICT (id) DO UPDATE SET email        = EXCLUDED.email,
                                   first_name   = EXCLUDED.first_name,
                                   last_name    = EXCLUDED.last_name,
                                   passport_id  = EXCLUDED.passport_id,
                                   phone_number = EXCLUDED.phone_number;
END;
$$;
