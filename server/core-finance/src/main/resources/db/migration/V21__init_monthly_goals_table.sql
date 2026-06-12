CREATE TABLE monthly_goals (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               goal_year INT NOT NULL,
                               goal_month INT NOT NULL CHECK (goal_month BETWEEN 1 AND 12),
                               target_amount NUMERIC(15, 2) NOT NULL CHECK (target_amount >= 0),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uq_user_monthly_goal UNIQUE (user_id, goal_year, goal_month)
);


CREATE OR REPLACE FUNCTION sp_upsert_monthly_goal(
    p_user_id UUID,
    p_year INT,
    p_month INT,
    p_amount NUMERIC(15, 2)
) RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
v_goal_id UUID;
BEGIN
INSERT INTO monthly_goals (user_id, goal_year, goal_month, target_amount, updated_at)
VALUES (p_user_id, p_year, p_month, p_amount, CURRENT_TIMESTAMP)
    ON CONFLICT (user_id, goal_year, goal_month)
    DO UPDATE SET
    target_amount = EXCLUDED.target_amount,
               updated_at = CURRENT_TIMESTAMP
               RETURNING id INTO v_goal_id;

RETURN v_goal_id;
END;
$$;
