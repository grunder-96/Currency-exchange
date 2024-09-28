INSERT INTO currencies(code, full_name, sign) VALUES
    ('USD', 'United States dollar', '$'),
    ('RUB', 'Russian ruble', '₽'),
    ('EUR', 'Euro', '€'),
    ('JPY', 'Japanese yen', '¥'),
    ('BYN', 'Belarusian ruble', 'Br'),
    ('BRL', 'Brazilian real', 'R$'),
    ('PLN', 'Polish złoty', 'zł'),
    ('TRY', 'Turkish lira', '₺'),
    ('UAH', 'Ukrainian hryvnia', '₴'),
    ('GEL', 'Georgian lari', 'ლარი');

INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES
    (1, 2, 92.71),
    (1, 3, 0.8956),
    (2, 9, 0.444476),
    (7, 10, 0.712803),
    (1, 4, 142.18),
    (6, 8, 6.29),
    (1, 5, 3.21),
    (5, 9, 12.9);