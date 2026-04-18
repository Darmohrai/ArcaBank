CREATE OR REPLACE PROCEDURE sp_sync_user(
    p_id VARCHAR,
    p_email VARCHAR,
    p_first_name VARCHAR,
    p_last_name VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO users (id, email, first_name, last_name)
VALUES (p_id, p_email, p_first_name, p_last_name)
    ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
                            first_name = EXCLUDED.first_name,
                            last_name = EXCLUDED.last_name;
END;
$$;
