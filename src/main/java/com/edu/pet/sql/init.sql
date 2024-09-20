CREATE TABLE  IF NOT EXISTS currencies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE CHECK (length(code) = 3) COLLATE NOCASE,
    full_name TEXT NOT NULL UNIQUE CHECK (length(full_name) BETWEEN 1 AND 32),
    sign TEXT NOT NULL CHECK (length(sign) BETWEEN 1 AND 3)
) STRICT;

CREATE TABLE IF NOT EXISTS exchange_rates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id INTEGER NOT NULL,
    target_currency_id INTEGER NOT NULL,
    rate REAL NOT NULL CHECK (rate > 0),
    FOREIGN KEY (base_currency_id) REFERENCES currencies(id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies(id)
) STRICT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_base_currency_id_target_currency_id
    ON exchange_rates(base_currency_id, target_currency_id);