INSERT INTO notifications (user_id, type, title, message, is_read, reference_id) VALUES 
(
    '11111111-1111-1111-1111-111111111111', 
    'INCOMING_TRANSFER', 
    'Зарахування коштів', 
    '+1500.00 UAH від Леся У.', 
    FALSE, 
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    '11111111-1111-1111-1111-111111111111', 
    'SECURITY', 
    'Безпека', 
    'Вхід у систему з нового пристрою: iPhone 17 Pro Max', 
    TRUE, 
    NULL
);

INSERT INTO notifications (user_id, type, title, message, is_read)
VALUES 
(
    '22222222-2222-2222-2222-222222222222', 
    'SECURITY', 
    'Пароль змінено', 
    'Ваш пароль було успішно оновлено. Якщо це були не ви, зверніться в підтримку.', 
    FALSE
);