INSERT INTO accounts (id, user_id, iban, type, currency, balance) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'UA893052990000026000000000001', 'CHECKING', 'UAH', 15000.50),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'UA893052990000026000000000002', 'SAVINGS', 'UAH', 50000.00),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 'UA893052990000026000000000003', 'CHECKING', 'UAH', 250.00);

INSERT INTO cards (account_id, card_number, card_holder_name, expiration_date, cvv_hash) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '4149111122223333', 'TARAS SHEVCHENKO', '12/28', '$2a$12$wL8r4kX...'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '4149444455556666', 'LESYA UKRAINKA', '05/27', '$2a$12$wL8r4kX...'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '4149777788889999', 'IVAN FRANKO', '09/29', '$2a$12$wL8r4kX...');

INSERT INTO transactions (sender_account_id, receiver_account_id, amount, currency, status) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 1500.00, 'UAH', 'SUCCESS'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 200.00, 'UAH', 'SUCCESS'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 50.00, 'UAH', 'PENDING');

-- default ID приблизно такі будуть a3f1c2d4-7b9e-4f12-8c3a-9d5e6f7a8b90
-- але для тестування будемо використовувати фіксовані UUID