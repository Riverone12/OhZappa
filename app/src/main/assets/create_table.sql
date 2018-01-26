CREATE TABLE IF NOT EXISTS account_item(id INTEGER PRIMARY KEY, title TEXT, closing INTEGER, settlement INTEGER, color INTEGER)
CREATE TABLE IF NOT EXISTS payment_item(id INTEGER PRIMARY KEY, account INTEGER, pay_date INTEGER, amount INTEGER, memo TEXT, registered INTEGER)
