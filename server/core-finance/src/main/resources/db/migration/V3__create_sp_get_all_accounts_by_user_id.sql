CREATE
    OR REPLACE FUNCTION sp_get_all_accounts_by_user_id(p_user_id UUID)
    RETURNS SETOF accounts
AS
$$
SELECT *
FROM accounts
WHERE user_id = p_user_id;
$$
    LANGUAGE sql STABLE;
