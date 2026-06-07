CREATE INDEX idx_notifications_user_date ON notifications(user_id, created_at DESC);


INSERT INTO notifications (id, user_id, notification_type, action_type, title, text, is_read, endpoint)
VALUES
(
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    'TRANSFER_INCOME',
    'EXTERNAL_LINK',
    'Зарахування коштів',
    '+1500.00 UAH від Леся У.',
    FALSE,
    '/transactions/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    gen_random_uuid(),
    '11111111-1111-1111-1111-111111111111',
    'TRANSFER_INCOME',
    'EXTERNAL_LINK',
    'Безпека',
    'Вхід у систему з нового пристрою: iPhone 17 Pro Max',
    TRUE,
    NULL
);

INSERT INTO notifications (id, user_id, notification_type, action_type, title, text, is_read, endpoint)
VALUES
(
    gen_random_uuid(),
    '22222222-2222-2222-2222-222222222222',
    'TRANSFER_INCOME',
    'EXTERNAL_LINK',
    'Пароль змінено',
    'Ваш пароль було успішно оновлено. Якщо це були не ви, зверніться в підтримку.',
    FALSE,
    NULL
);
