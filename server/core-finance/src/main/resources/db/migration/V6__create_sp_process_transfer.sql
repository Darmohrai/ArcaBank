CREATE OR REPLACE FUNCTION sp_process_transfer(
    p_sender_id UUID,
    p_receiver_id UUID,
    p_amount NUMERIC(15, 2)
) RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_sender_record RECORD;
    v_receiver_record RECORD;
    v_transaction_id UUID;
BEGIN
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Amount must be greater than zero' USING ERRCODE = 'P0001';
    END IF;

    IF p_sender_id = p_receiver_id THEN
        RAISE EXCEPTION 'Cannot transfer to the same account' USING ERRCODE = 'P0002';
    END IF;

    IF p_sender_id < p_receiver_id THEN
        SELECT * INTO v_sender_record FROM accounts WHERE id = p_sender_id FOR UPDATE;
        SELECT * INTO v_receiver_record FROM accounts WHERE id = p_receiver_id FOR UPDATE;
    ELSE
        SELECT * INTO v_receiver_record FROM accounts WHERE id = p_receiver_id FOR UPDATE;
        SELECT * INTO v_sender_record FROM accounts WHERE id = p_sender_id FOR UPDATE;
    END IF;


    IF v_sender_record IS NULL OR v_sender_record.status != 'ACTIVE' THEN
        RAISE EXCEPTION 'Sender account does not exist or is not active' USING ERRCODE = 'P0003';
    END IF;

    IF v_receiver_record IS NULL OR v_receiver_record.status != 'ACTIVE' THEN
        RAISE EXCEPTION 'Receiver account does not exist or is not active' USING ERRCODE = 'P0004';
    END IF;

    IF v_sender_record.balance < p_amount THEN
        RAISE EXCEPTION 'Insufficient funds' USING ERRCODE = 'P0005';
    END IF;

    IF v_sender_record.currency != v_receiver_record.currency THEN
        RAISE EXCEPTION 'Currency mismatch between accounts' USING ERRCODE = 'P0006';
    END IF;

   
    UPDATE accounts 
    SET balance = balance - p_amount 
    WHERE id = p_sender_id;

    UPDATE accounts 
    SET balance = balance + p_amount 
    WHERE id = p_receiver_id;

    INSERT INTO transactions (sender_account_id, receiver_account_id, amount, currency, status)
    VALUES (p_sender_id, p_receiver_id, p_amount, v_sender_record.currency, 'SUCCESS')
    RETURNING id INTO v_transaction_id;

    RETURN v_transaction_id;
END;
$$;